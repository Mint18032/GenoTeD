package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.normalizer.ParameterComparator;
import io.testrest.parser.EditReadOnlyOperationException;
import io.testrest.helper.ObjectHelper;
import io.testrest.helper.Taggable;
import io.testrest.datatype.graph.OperationNode;

import java.util.logging.Logger;

import java.util.*;

/*
 * In this class every function that sets a value after the Parameter creation checks the value of 'isReadOnly' field
 * in the associated operation: if true, it won't allow any modification and will throw an exception.
 * This behavior has been implemented to prevent accidental modifications to the template structure of the reference
 * OpenAPI specification. In fact, every operation (and consequently its parameters) parsed from the specification is
 * set as read-only. In order to be able to perform any modification, the operation must be cloned.
 */
public abstract class ParameterElement extends Taggable {

    protected ParameterName name;
    protected NormalizedParameterName normalizedName;
    protected String schemaName; // Name of the referred schema, if any; else null
    protected boolean required;
    protected ParameterType type;
    protected ParameterTypeFormat format;
    private ParameterLocation location; // Position of the parameter (e.g. path, header, query, etc. )
    private ParameterStyle style;
    private boolean explode;

    protected Object defaultValue;
    protected HashSet enumValues;
    protected Set<Object> examples;

    private String description;

    private OperationNode operation; // Operation to which the parameter is associated
    private ParameterElement parent; // Reference to the parent Parameter if any; else null

    private static final String castedWarn = "' was not compliant to parameter type, but it has been " +
            "cast to fit the right type.";
    private static final String discardedWarn = "' is not compliant to parameter type. The value will be discarded.";
    private static final Logger logger = Logger.getLogger(ParameterElement.class.getName());

    public ParameterElement(){}

    public ParameterElement(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        if (name != null) {
            this.name = new ParameterName(name);
        } else if (parameterMap.getName() == null) {
            throw new ParameterCreationException("Missing name for parameter in operation '" + operation +
                    "' (parent: " + parent + ").");
        } else {
            this.name = new ParameterName((String) parameterMap.getName());
        }

        // Difference between parameter and request body/response body
        // Parameters can have a schema definition which contains type, format, default and enum values
        @SuppressWarnings("unchecked")
        Schema schema = parameterMap.getSchema(); //sourceMap

        this.schemaName = schema.getName();

        this.operation = operation;
        this.parent = parent;
        this.required = parameterMap.getRequired();
        this.location = ParameterLocation.getLocationFromString(parameterMap.getIn());
        // If style is absent apply default by OpenAPI standard
        this.style = ParameterStyle.getStyleFromString(parameterMap.getStyle().toString());
        if (style == null) {
            switch (this.location) {
                case HEADER:
                    operation.setContainsHeader(true);
                case PATH:
                    this.style = ParameterStyle.SIMPLE;
                    break;
                case QUERY:
                case COOKIE:
                default:
                    this.style = ParameterStyle.FORM;
            }
        }

        Boolean specExplode = parameterMap.getExplode();
        this.explode = Objects.requireNonNullElseGet(specExplode, () -> this.style == ParameterStyle.FORM);

        this.type = ParameterType.getTypeFromString(schema.getType());
        this.format = ParameterTypeFormat.getFormatFromString(schema.getType());

        this.description = parameterMap.getDescription();

        Object defaultValue = schema.getDefault();
        if (defaultValue != null) {
            if (isObjectTypeCompliant(defaultValue)) {
                this.defaultValue = defaultValue;
            } else {
                try {
                    this.defaultValue = ObjectHelper.castToParameterValueType(defaultValue, type);
                    logger.warning("Default value " + defaultValue + castedWarn);
                } catch (ClassCastException e) {
                    this.defaultValue = null;
                    logger.warning("Default value " + defaultValue + discardedWarn);
                }
            }
        }

        this.enumValues = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<Object> values = schema.getEnum();
        values.forEach(value -> {
            if (isObjectTypeCompliant(value)) {
                this.enumValues.add(value);
            } else {
                try {
                    this.enumValues.add(ObjectHelper.castToParameterValueType(value, type));
                    logger.warning("Enum value '" + value + castedWarn);
                } catch (ClassCastException e) {
                    logger.warning("Enum value '" + value + discardedWarn);
                }
            }
        });

        this.examples = new HashSet<>();
        // Example and examples should be mutually exclusive. Moreover, examples field is not allowed in request bodies.
        // The specification is parsed in a more relaxed way, pursuing fault tolerance and flexibility.
        Object exampleValue = parameterMap.getExample();
        if (exampleValue != null) {
            if (isObjectTypeCompliant(exampleValue)) {
                this.examples.add(exampleValue);
            } else {
                try {
                    this.examples.add(ObjectHelper.castToParameterValueType(exampleValue, type));
                    logger.warning("Example value " + exampleValue + " was not compliant to parameter type, but it has been " +
                            "cast to fit the right type.");
                } catch (ClassCastException e) {
                    logger.warning("Example value " + exampleValue + " is not compliant to parameter type. " +
                            "The value will be discarded.");
                }
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Example> examples = parameterMap.getExamples();
        examples.values().forEach(example -> {
            if (example.getValue() != null) {
                Object value = example.getValue();
                if (isObjectTypeCompliant(value)) {
                    this.examples.add(value);
                } else {
                    try {
                        this.examples.add(ObjectHelper.castToParameterValueType(value, type));
                        logger.warning("Example value " + value + castedWarn);
                    } catch (ClassCastException e) {
                        logger.warning("Example value " + value + discardedWarn);
                    }
                }
            } else if (example.getExternalValue() != null) {
                logger.warning("Examples containing external values are not currently supported.");
            }
        });

        this.normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
    }

    public ParameterElement (Parameter parameterMap, OperationNode operation, String name) {
        this(null, parameterMap, operation, name);
    }

    /*
     * Copy constructors used to clone parameters. They are declared as protected to force the use of the function
     * deepCopy externally.
     */
    protected ParameterElement(Parameter other) {
        name = new ParameterName(other.getName());
        schemaName = other.getSchema().getName();
        required = other.getRequired();
        type = ParameterType.getTypeFromString(other.getSchema().getType());
        format = ParameterTypeFormat.getFormatFromString(other.getSchema().getFormat());
        location = ParameterLocation.getLocationFromString(other.getIn());
        style = ParameterStyle.getStyleFromString(other.getStyle().toString());
        explode = other.getExplode();

        description = other.getDescription();

        defaultValue = ObjectHelper.deepCloneObject(other.getSchema().getDefault());
        enumValues = new HashSet<>();
        if (other.getSchema().getEnum() != null) {
            other.getSchema().getEnum().forEach(p -> enumValues.add(p));
        }
        if (other.getExamples() != null) {
            examples = new HashSet<>(ObjectHelper.deepCloneObject(other.getExamples().entrySet()));
        }

//        parent = other.parent;
//        tags.addAll(other.tags);
//        normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
    }

    /*
     * Used to create wrong type version of a parameter.
     */
    protected ParameterElement(ParameterElement other) {
        name = other.getName();
        required = other.isRequired();
        location = other.getLocation();
        style = other.getStyle();
        explode = other.isExplode();
        operation = other.getOperation();

        description = other.getDescription();
        enumValues = (HashSet) other.getEnumValues();
    }

    protected ParameterElement(Parameter other, OperationNode operation) {
        this.operation = operation;
        name = new ParameterName(other.getName());
        schemaName = other.getSchema().getName();
        required = other.getRequired() != null ? other.getRequired() : false;
        type = ParameterType.getTypeFromString(other.getSchema().getType());
        format = ParameterTypeFormat.getFormatFromString(other.getSchema().getFormat());
        location = ParameterLocation.getLocationFromString(other.getIn());
        if (other.getStyle() != null)
            style = ParameterStyle.getStyleFromString(other.getStyle().toString());
        explode = other.getExplode() != null ? other.getExplode() : false;

        description = other.getDescription();

        defaultValue = ObjectHelper.deepCloneObject(other.getSchema().getDefault());
        enumValues = new HashSet<>();
        if (other.getSchema().getEnum() != null) {
            other.getSchema().getEnum().forEach(p -> enumValues.add(p));
        }
        if (other.getExamples() != null) {
            examples = new HashSet<>(ObjectHelper.deepCloneObject(other.getExamples().entrySet()));
        }

//        parent = other.parent;
//        tags.addAll(other.tags);
        normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
    }

    protected ParameterElement(Parameter other, OperationNode operation, ParameterElement parent) {
        this(other);

        this.normalizedName = new NormalizedParameterName(other.getName());
        this.operation = operation;
        this.parent = parent;

//        tags.addAll(other.tags);
    }

    protected ParameterElement(OperationNode operation, ParameterElement parent) {
        this.operation = operation;
        this.parent = parent;
        this.location = ParameterLocation.RESPONSE_BODY;
        this.explode = false;
        this.description = "";
        this.enumValues = new HashSet<>();
        this.examples = new HashSet<>();
    }

    public abstract ParameterElement merge(ParameterElement other);

    /**
     * To remove the parameter from the parent element (or operation).
     */
//    public abstract boolean remove();

    /**
     * To replace a parameter with another one.
     * @param newParameter the new parameter to put.
     * @return true if the replacement could be completed.
     */
    /*
    public boolean replace(ParameterElement newParameter) {

        newParameter.setOperation(this.getOperation());
        newParameter.setParent(this.getParent());
        newParameter.setLocation(this.getLocation());

        // If the parameter has no parent (it is a root)
        if (getParent() == null) {
            switch (getLocation()) {
                case REQUEST_BODY:
                    if (this.description.equals(getOperation().getRequestBody().getDescription())) {
                        if (newParameter instanceof StructuredParameterElement) {
                            getOperation().setRequestBody((StructuredParameterElement) newParameter);
                            return true;
                        } else {
                            return false;
                        }
                    }
                    break;
                case RESPONSE_BODY:
                    if (this == getOperation().getResponseBody()) {
                        if (newParameter instanceof StructuredParameterElement) {
                            getOperation().setResponseBody((StructuredParameterElement) newParameter);
                            return true;
                        } else {
                            return false;
                        }
                    }
                    break;
                case QUERY:
                    if (getOperation().getQueryParameters().contains(this)) {
                        return getOperation().getQueryParameters().remove(this) && getOperation().getQueryParameters().add(newParameter);
                    }
                    return false;
                case PATH:
                    if (getOperation().getPathParameters().contains(this)) {
                        return getOperation().getPathParameters().remove(this) && getOperation().getQueryParameters().add(newParameter);
                    }
                    return false;
                case HEADER:
                    if (getOperation().getHeaderParameters().contains(this)) {
                        return getOperation().getHeaderParameters().remove(this) && getOperation().getQueryParameters().add(newParameter);
                    }
                    return false;
                case COOKIE:
                    if (getOperation().getCookieParameters().contains(this)) {
                        return getOperation().getCookieParameters().remove(this) && getOperation().getQueryParameters().add(newParameter);
                    }
                    return false;
            }
        }

        // If the parameter is contained in a parent element (array or object), remove it from the parent
        else {
            if (getParent() instanceof ArrayParameter) {
                ((ArrayParameter) getParent()).getElements().remove(this);
                ((ArrayParameter) getParent()).getElements().add(newParameter);
                return true;
            } else if (getParent() instanceof ObjectParameter) {
                ((ObjectParameter) getParent()).getProperties().remove(this);
                ((ObjectParameter) getParent()).getProperties().add(newParameter);
                return true;
            }
        }

        return false;
    }*/

    /**
     * Function to check whether the object passed as parameter is compliant to the Parameter type.
     * Each ParameterElement subclass implements it checking the type against the one that it expects for its
     * values/enum values/examples/etc.
     * @param o The object to be checked for compliance
     * @return True if o is compliant to the Parameter; false otherwise
     */
    public abstract boolean isObjectTypeCompliant(Object o);

    /**
     * Method to retrieve the heading for the JSON string. It was implemented to avoid errors caused by a missing
     * parameter name.
     */
    protected String getJSONHeading() {
        return name == null || name.toString().equals("") ? "" : "\"" + name + "\": ";
    }

    /**
     * Method to get the parameter as a JSON string. It can be used to construct JSON request bodies.
     * @return the JSON string.
     */
    public abstract String getJSONString(Object value);

    /**
     * Returns the JSON path for the element, e.g., owner.name
     * @return the JSON path for the element, e.g., owner.name
     */
    public abstract String getJsonPath();

    public abstract ParameterElement getParameterFromJsonPath(String jsonPath);

    /**
     * Function to retrieve the value of a Parameter as a string accordingly to given style and explode
     * @param style Describes how the parameter value will be serialized depending on the type of the parameter value
     * @param explode Parameter to change the way a specific style is rendered
     * @return A string with the rendered value
     */
    public abstract String getValueAsFormattedString (ParameterStyle style, boolean explode, Object value);

    /**
     * Shorthand for getValueAsFormattedString where the value of 'explode' is the same of the instance one.
     * @param style the style to be used for the rendering.
     * @return a string with the rendered value.
     */
    public String getValueAsFormattedString (ParameterStyle style, Object value) {
        return getValueAsFormattedString (style, this.explode, value);
    }

    /**
     * Shorthand for getValueAsFormattedString where the values of 'style' and 'explode' are the ones of the instance.
     * This function can be used to get the default rendering of a Parameter.
     * @return A string with the rendered value
     */
    public String getValueAsFormattedString (Object value) {
        return getValueAsFormattedString(this.style, this.explode, value);
    }

    public ParameterStyle getStyle() {
        return style;
    }

    public void setStyle(ParameterStyle style) {
        this.style = style;
    }

    public boolean isExplode() {
        return explode;
    }

    public void setExplode(boolean explode) {
        this.explode = explode;
    }

    public final boolean isEnum() {
        return !this.enumValues.isEmpty();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean setDefaultValue(Object defaultValue) {
        if (operation.isReadOnly()) {
            throw new EditReadOnlyOperationException(operation);
        }
        if (this.isObjectTypeCompliant(defaultValue)) {
            this.defaultValue = defaultValue;
        } else {
            try {
                this.defaultValue = ObjectHelper.castToParameterValueType(defaultValue, this.type);
                logger.warning("Example value '" + defaultValue + castedWarn);
            } catch (ClassCastException e) {
                logger.warning("Example value '" + defaultValue + discardedWarn);
                return false;
            }
        }
        return true;
    }

    public Set<Object> getEnumValues() {
        return enumValues;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    public NormalizedParameterName getNormalizedName() {
        return this.normalizedName;
    }

    public ParameterName getName() {
        return name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean addExample(Object o) {
        if (operation.isReadOnly()) {
            throw new EditReadOnlyOperationException(operation);
        }
        if (this.isObjectTypeCompliant(o)) {
            this.examples.add(o);
        } else {
            try {
                this.examples.add(ObjectHelper.castToParameterValueType(o, this.type));
                logger.warning("Example value '" + o + castedWarn);
            } catch (ClassCastException e) {
                logger.warning("Example value '" + o + discardedWarn);
                return false;
            }
        }
        return true;
    }

    public Set<Object> getExamples() {
        return examples;
    }

    public ParameterType getType() {
        return type;
    }

    public ParameterTypeFormat getFormat() {
        return format;
    }

    public void setFormat(ParameterTypeFormat format) {
        // FIXME: move set format to leaves to check that type matches the format
        this.format = format;
    }

    public OperationNode getOperation() {
        return operation;
    }

    public void setOperation(OperationNode operation) {
        this.operation = operation;
    }

    public ParameterLocation getLocation() {
        return location;
    }

    public void setLocation(ParameterLocation location) {
        this.location = location;
    }

    protected void setNormalizedName(NormalizedParameterName normalizedName) {
        if (operation.isReadOnly()) {
            throw new EditReadOnlyOperationException(operation);
        }
        this.normalizedName = normalizedName;
    }

    @Override
    public String toString() {
        return this.name + " (" + normalizedName + ", " + location + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterElement parameter = (ParameterElement) o;

        return Objects.equals(name, parameter.name) &&
                Objects.equals(type, parameter.type) &&
                Objects.equals(location, parameter.location) &&
                Objects.equals(operation, parameter.operation) &&
                // If even one of the parameters has null parent, then ignore normalized name. Else, consider it.
                // This behaviour is to restrict the most possible the use of normalizedName in equals
                (parent == null || parameter.parent == null || Objects.equals(normalizedName, parameter.normalizedName));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, location, operation);
    }

    /**
     * Returns the root element of a structured parameter. If a parameter is not structured, then the root element is
     * itself.
     * @return the root element of a structured parameter.
     */
    public ParameterElement getRoot() {
        if (getParent() == null) {
            return this;
        } else {
            return getParent().getRoot();
        }
    }

    public ParameterElement getParent() {
        return parent;
    }

    public void setParent(ParameterElement parent) {
        this.parent = parent;

        // Also update operation of the parameter, to match the one of the new parent
        // FIXME: operation should be updated also when parent == null
        if (parent != null) {
            this.operation = parent.getOperation();
        }
    }

    /**
     * Clones the parameter by creating its exact, deep copy
     * @return deep copy of the parameter
     */
    public abstract ParameterElement deepClone();

    /**
     * Creates a deep copy of the parameter modifying its reference operation and parameters.
     * This function is mainly used when cloning an operation since the cloned parameters must reference to the new
     * operation instead referencing the same operation of the original parameter. New parent is necessary for the same
     * reason, since structured parameters need to give the clones of their elements/properties the reference to
     * themselves instead to the old parent.
     * @param operation New operation to be referenced.
     * @param parent New parent to be referenced.
     * @return the cloned parameter.
     */
    public abstract ParameterElement deepClone(OperationNode operation, ParameterElement parent);

    /**
     * Return a collection containing the arrays in the parameter element and underlying elements.
     * @return the collection of arrays in the parameter.
     */
    public abstract Collection<ArrayParameter> getArrays();

    /**
     * Return a collection containing the objects in the parameter element and underlying elements.
     * @return the collection of objects in the parameter.
     */
    public abstract Collection<ObjectParameter> getObjects();

    /**
     * Return a collection containing the objects in the parameter element and underlying elements. In case of arrays,
     * only the objects in reference element are returned.
     * @return the collection of objects in the parameter.
     */
    public abstract Collection<ObjectParameter> getReferenceObjects();

    /**
     * Returns all parameters elements of this element.
     * @return all parameters elements of this element.
     */
    public abstract Collection<ParameterElement> getAllParameters();

    /**
     * Returns a collection containing the leaves in the parameter element and underlying elements.
     * @return the collection of leaves in the parameter
     */
    public abstract Collection<ParameterLeaf> getLeaves();

    /**
     * Get the leaves of the parameters. In case of arrays, the returned leaves are part of the reference element of the
     * array.
     * @return the reference leaves in the parameter.
     */
    public abstract Collection<ParameterLeaf> getReferenceLeaves();

    /**
     * Returns a collection containing the combined schemas in the parameter element and underlying elements.
     * @return the collection of combined schemas in the parameter
     */
    public abstract Collection<CombinedSchemaParameter> getCombinedSchemas();

    public abstract boolean isSet();

    public boolean isObject() {
        return this instanceof ObjectParameter;
    }

    public boolean isArray() {
        return this instanceof ArrayParameter;
    }

    public boolean isArrayOfLeaves() {
        return this instanceof ArrayParameter && ((ArrayParameter) this).getReferenceElement() instanceof ParameterLeaf;
    }

    public boolean isArrayOfArrays() {
        return this instanceof ArrayParameter && ((ArrayParameter) this).getReferenceElement() instanceof ArrayParameter;
    }

    public boolean isArrayOfObjects() {
        return this instanceof ArrayParameter && ((ArrayParameter) this).getReferenceElement() instanceof ObjectParameter;
    }

    public boolean isLeaf() {
        return this instanceof ParameterLeaf;
    }

    public boolean isNumber() {
        return this instanceof NumberParameter;
    }

    public boolean isString() {
        return this instanceof StringParameter;
    }

    public boolean isBoolean() {
        return this instanceof BooleanParameter;
    }

    public boolean isNull() {
        return this instanceof NullParameter;
    }

    /**
     * Check if a parameter element is a reference element of an array parameter.
     * @return true if parameter is reference element.
     */
    public boolean isReferenceElement() {
        return parent != null && parent instanceof ArrayParameter && ((ArrayParameter) parent).getReferenceElement() == this;
    }

    /**
     * Check if a parameter is an element of an array parameter.
     * @return true if parameter is an element of an array parameter.
     */
    public boolean isArrayElement() {
        return parent != null && parent instanceof ArrayParameter && ((ArrayParameter) parent).getElements().contains(this);
    }

    /**
     * Check if a parameter is a property of an object.
     * @return true if parameter is property of an object.
     */
    public boolean isObjectProperty() {
        return parent != null && parent instanceof ObjectParameter && ((ObjectParameter) parent).getProperties().contains(this);
    }
}

