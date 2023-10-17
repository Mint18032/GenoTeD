package io.testrest.datatype.parameter;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.parser.EditReadOnlyOperationException;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.parser.UnsupportedSpecificationFeature;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ObjectParameter extends StructuredParameterElement {

    /**
     * We keep track of the order of the properties inside objects, but we do not consider it in the equals/hash
     * functions since it should have no importance.
     * From a testing perspective, instead, it could be useful to discover implementation defects.
     */
    private List<ParameterElement> properties;

    private static final Logger logger = Logger.getLogger(ObjectParameter.class.getName());

    @SuppressWarnings("unchecked")
    public ObjectParameter(ParameterElement parent, Parameter schema, OperationNode operation, String name)  {
        super(parent, schema, operation, name);
        this.properties = new LinkedList<>();

        setNormalizedName(NormalizedParameterName.computeParameterNormalizedName(this));

        Map<String, Object> properties = schema.getSchema().getProperties();

        ParameterElement parameterElement;
        for (Map.Entry<String, Object> propertyMap : properties.entrySet()) {
            try {
                Schema map = (Schema) propertyMap.getValue();
                Parameter param = schema;
                param.setSchema(map);
                param.setName(propertyMap.getKey());
                parameterElement = ParameterFactory.getParameterElement(this, param, operation, propertyMap.getKey());
                this.properties.add(parameterElement);

                // Propagate example values to children
                for (Object example : super.examples) {
                    Map<String, Object> exampleMap = (Map<String, Object>) example;
                    Object exampleValue = exampleMap.get(parameterElement.getName().toString());
                    if (exampleValue != null) {
                        parameterElement.addExample(exampleValue);
                    }
                }
            } catch (UnsupportedSpecificationFeature e) {
                logger.warning("Skipping property '" + propertyMap.getKey() + "' in object '" + name + "' due to " +
                        "an unsupported feature in OpenAPI specification.");
            }
        }
    }

    public ObjectParameter(Parameter other) {
        super(other);

        properties = new LinkedList<>();
//        TODO: map object properties
        other.getSchema().getProperties();
    }

    public ObjectParameter(Parameter other, OperationNode operation) {
        super(other, operation);

        properties = new LinkedList<>();
//        TODO: map object properties
        other.getSchema().getProperties();
    }

    private ObjectParameter(ObjectParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);

        properties = new LinkedList<>();
        other.properties.forEach(p -> properties.add(p.deepClone(operation, this)));
    }

    public ObjectParameter(JsonObject jsonObject, OperationNode operation, ParameterElement parent, String name) {
        super(operation, parent);

        this.properties = new LinkedList<>();
        this.name = new ParameterName(Objects.requireNonNullElse(name, ""));
        this.normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
        this.type = ParameterType.OBJECT;

        for (String entryName : jsonObject.keySet()) {
            ParameterElement p =
                    ParameterFactory.getParameterElement(this, jsonObject.get(entryName), operation, entryName);
            if (p != null) {
                properties.add(p);
            }
        }
    }

    @Override
    public ObjectParameter merge(ParameterElement other) {
        if (!(other instanceof ObjectParameter)) {
            throw new IllegalArgumentException("Cannot merge a " + this.getClass() + " instance with a "
                    + other.getClass() + " one.");
        }

        ObjectParameter parameterObject = (ObjectParameter) other;
        ObjectParameter merged = this;

        merged.properties = this.mergeProperties(parameterObject.properties);

        return merged;
    }

    private List<ParameterElement> mergeProperties(List<ParameterElement> other) {

        List<ParameterElement> merged = new LinkedList<>();
        HashSet<ParameterName> commonNames = this.properties.stream().map(ParameterElement::getName)
                .collect(Collectors.toSet()).stream().filter(p -> other.stream().map(ParameterElement::getName)
                        .collect(Collectors.toSet()).contains(p))
                .collect(Collectors.toCollection(HashSet::new));

        this.properties.forEach(p -> {
            if (commonNames.contains(p.getName())) {
                merged.add(p.merge(other.stream().filter(p2 -> p2.getName().equals(p.getName())).findFirst().get()));
            } else {
                merged.add(p);
            }
        });

        merged.addAll(other.stream().filter(p -> !commonNames.contains(p.getName())).collect(Collectors.toSet()));

        return merged;
    }

    @Override
    public boolean isObjectTypeCompliant(Object o) {
        if (o == null) {
            return false;
        }
        return Map.class.isAssignableFrom(o.getClass());
    }

    @Override
    public String getJSONString() {
        StringBuilder stringBuilder = new StringBuilder();

        // If object is inside an array, no heading is printed
        if (getParent() instanceof ArrayParameter) {
            stringBuilder.append("{");
        } else {
            stringBuilder.append(getJSONHeading()).append("{");
        }
        properties.forEach(p -> stringBuilder.append(p.getJSONString()).append(", "));
        int index = stringBuilder.lastIndexOf(",");
        return stringBuilder.substring(0, index > 0 ? index : stringBuilder.length()) + "}";
    }

    @Override
    public String toString() {
        //StringBuilder stringBuilder = new StringBuilder(super.getNormalizedName() + ": {");
        StringBuilder stringBuilder = new StringBuilder(getNormalizedName() + ": {");
        properties.forEach(p -> stringBuilder.append(p.toString()).append(", "));
        int index = stringBuilder.lastIndexOf(",");
        return stringBuilder.substring(0, index > 0 ? index : stringBuilder.length()) + "}";
    }

    public List<ParameterElement> getProperties() {
        if (getOperation().isReadOnly()) {
            return Collections.unmodifiableList(properties);
        }
        return properties;
    }

    @Override
    public boolean addExample(Object o) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }

        if (!super.addExample(o)) {
            return false;
        }

        // Propagate example values to children
        Map<String, Object> exampleMap = (Map<String, Object>) o;
        for (Map.Entry<String, Object> example : exampleMap.entrySet()) {
            properties.stream().filter(p -> p.getName().toString().equals(example.getKey()))
                    .findFirst().ifPresent(
                            parameterElement -> parameterElement.addExample(example.getValue())
                    );
        }

        return true;
    }

    public void addProperty(ParameterElement property) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }

        List<ParameterElement> prop = new LinkedList<>();
        prop.add(property);
        this.properties = this.mergeProperties(prop);
    }

    public void addProperties(List<ParameterElement> properties) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }

        this.properties = this.mergeProperties(properties);
    }

    // TODO: create function to remove from a StringBuilder last matching chars? Maybe in a Helper class
    public String getValueAsFormattedString (ParameterStyle style, boolean explode) {
        logger.warning("Format for deep nested object is not defined in the reference RFC. Use this method only for " +
                "RFC defined behaviors.");
        StringBuilder stringBuilder = new StringBuilder();
        switch (style) {
            case MATRIX:
                if (explode) {
                    // ;R=100;G=200;B=150
                    properties.forEach(p -> {
                        stringBuilder.append(";");
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    });
                } else {
                    // ;color=R,100,G,200,B,150
                    stringBuilder.append(";");
                    stringBuilder.append(getName().toString());
                    stringBuilder.append("=");
                    properties.forEach(p -> {
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append(",");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                }
                return stringBuilder.toString();

            case LABEL:
                if (explode) {
                    // 	.R=100.G=200.B=150
                    properties.forEach(p -> {
                        stringBuilder.append(".");
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    });
                } else {
                    // .R.100.G.200.B.150
                    properties.forEach(p -> {
                        stringBuilder.append(".");
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append(".");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    });
                }
                return stringBuilder.toString();

            case FORM:
                if (explode) {
                    // R=100&G=200&B=150
                    properties.forEach(p -> {
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append("&");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == '&') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                } else {
                    // color=R,100,G,200,B,150
                    stringBuilder.append(this.getName().toString());
                    stringBuilder.append("=");
                    properties.forEach(p -> {
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append(",");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                }
                return stringBuilder.toString();

            case SIMPLE:
                if (explode) {
                    // R=100,G=200,B=150
                    properties.forEach(p -> {
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                } else {
                    // R,100,G,200,B,150
                    properties.forEach(p -> {
                        stringBuilder.append(p.getName().toString());
                        stringBuilder.append(",");
                        stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                }
                if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            case SPACE_DELIMITED:
                // R%20100%20G%20200%20B%20150
                properties.forEach(p -> {
                    stringBuilder.append(p.getName().toString());
                    stringBuilder.append("%20");
                    stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append("%20");
                });
                if (stringBuilder.charAt(stringBuilder.length() - 1) == '0') {
                    return stringBuilder.substring(0, stringBuilder.length() - 3);
                }
                return stringBuilder.toString();

            case PIPE_DELIMITED:
                // R|100|G|200|B|150
                properties.forEach(p -> {
                    stringBuilder.append(p.getName().toString());
                    stringBuilder.append("|");
                    stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append("|");
                });
                if (stringBuilder.charAt(stringBuilder.length() - 1) == '|') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            case DEEP_OBJECT:
                // color[R]=100&color[G]=200&color[B]=150
                properties.forEach(p -> {
                    stringBuilder.append(this.getName().toString());
                    stringBuilder.append("[");
                    stringBuilder.append(p.getName().toString());
                    stringBuilder.append("]=");
                    stringBuilder.append(p.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append("&");
                });
                if (stringBuilder.charAt(stringBuilder.length() - 1) == '&') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            default:
                logger.warning(getName() + ": Style not consistent with parameter type. Returning " + getStyle() + " style.");
                return getValueAsFormattedString();
        }
    }

    public boolean hasValue() {
        boolean hasValue = true;

        for (ParameterElement element : properties) {
            hasValue = hasValue && element.hasValue();
        }

        if (!hasValue) {
            logger.warning("Parameter " + getName() + " has an invalid value.");
        }

        return hasValue;
    }

    @Override
    public List<ParameterLeaf> getLeaves() {
        List<ParameterLeaf> leaves = new LinkedList<>();

        for (ParameterElement property : properties) {
            leaves.addAll((property).getLeaves());
        }

        return leaves;
    }

    @Override
    public List<ParameterLeaf> getReferenceLeaves() {
        List<ParameterLeaf> leaves = new LinkedList<>();

        for (ParameterElement property : properties) {
            leaves.addAll(property.getReferenceLeaves());
        }

        return leaves;
    }

    /**
     * A ObjectParameter is considered empty when it has no property.
     * @return True if the instance has no property; false otherwise
     */
    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public void removeUninitializedParameters() {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }
        List<ParameterElement> newProperties = new LinkedList<>(this.properties);

        this.properties.stream().filter(
                p -> ParameterLeaf.class.isAssignableFrom(p.getClass()) && p.getValue() == null
        ).forEach(p -> {
            //logger.warn("Empty valued parameter '" + p.getName() + "' found. It will be removed.");
            newProperties.remove(p);
        });

        this.properties.stream().filter(
                p -> StructuredParameterElement.class.isAssignableFrom(p.getClass())
        ).forEach(p -> {
            StructuredParameterElement structuredP = (StructuredParameterElement) p;
            structuredP.removeUninitializedParameters();
            if (structuredP.isEmpty() && !structuredP.isKeepIfEmpty()) {
                //logger.warn("Empty valued parameter '" + p.getName() + "' found. It will be removed.");
                newProperties.remove(p);
            }
        });

        this.properties = newProperties;
    }

    @Override
    public int hashCode() {
        if (properties.isEmpty()) {
            return Objects.hash(super.hashCode(), getNormalizedName().hashCode());
        }
        Set<ParameterElement> propertiesSet = new HashSet<>(properties);
        return Objects.hash(super.hashCode(), propertiesSet);
    }

    @Override
    public ObjectParameter deepClone() {
        return this;
    }

    @Override
    public ObjectParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new ObjectParameter(this, operation, parent);
    }

    // Does not take into account properties order
    @Override
    public boolean equals(Object o) {
        return equalsTemplate(o, false);
    }

    // Takes into account properties order
    public boolean enforcedParameterOrderEquals(Object o) {
        return equalsTemplate(o, true);
    }

    private boolean equalsTemplate(Object o, boolean isParameterOrderEnforced) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ObjectParameter that = (ObjectParameter) o;
        if (properties.isEmpty()) {
            return getNormalizedName().equals(that.getNormalizedName());
        }
        if (!isParameterOrderEnforced) {
            Set<ParameterElement> propertiesSet = new HashSet<>(properties);
            Set<ParameterElement> thatPropertiesSet = new HashSet<>(that.properties);
            return propertiesSet.equals(thatPropertiesSet);
        }
        return properties.equals(that.properties);
    }

    /**
     * Returns itself, plus the arrays contained in its elements.
     * @return an empty list
     */
    @Override
    public Collection<ArrayParameter> getArrays() {
        Collection<ArrayParameter> arrays = new LinkedList<>();

        // For each property contained in the object, add their arrays.
        for (ParameterElement property : properties) {
            arrays.addAll(property.getArrays());
        }
        return arrays;
    }

    @Override
    public Collection<ObjectParameter> getObjects() {
        Collection<ObjectParameter> objects = new LinkedList<>();
        objects.add(this);
        properties.forEach(ParameterElement::getObjects);
        return objects;
    }

    @Override
    public Collection<ObjectParameter> getReferenceObjects() {
        Collection<ObjectParameter> objects = new LinkedList<>();
        objects.add(this);
        properties.forEach(ParameterElement::getReferenceObjects);
        return objects;
    }

    @Override
    public Collection<ParameterElement> getAllParameters() {
        HashSet<ParameterElement> parameters = new HashSet<>();
        parameters.add(this);
        properties.forEach(p -> parameters.addAll(p.getAllParameters()));
        return parameters;
    }

    @Override
    public Collection<CombinedSchemaParameter> getCombinedSchemas() {
        Collection<CombinedSchemaParameter> combinedSchemas = new LinkedList<>();

        for (ParameterElement element : properties) {
            combinedSchemas.addAll(element.getCombinedSchemas());
        }

        return combinedSchemas;
    }

    @Override
    public boolean isSet() {
        for (ParameterElement parameterElement : properties) {
            if (parameterElement.isSet()) {
                return true;
            }
        }
        return false;
    }

    private int getIndexOfParameterObject(){
        List<ParameterElement> elements = ((ArrayParameter) getParent()).getElements();
        int elementPosition=0;
        for(ParameterElement element : elements){
            if(this.getLeaves().equals(element.getLeaves())){
                return elementPosition;
            }
            elementPosition++;
        }
        return elementPosition;
    }

    /**
     * Return a parameter element according to its JSON path.
     * @param jsonPath the JSON path of the parameter we want to get.
     * @return the parameter matching the JSON path.
     */
    @Override
    public ParameterElement getParameterFromJsonPath(String jsonPath) {

        // If the JSON path starts with $, then start the search from the root element
        ParameterElement rootElement = getRoot();
        if (this != rootElement && jsonPath.startsWith("$")) {
            return rootElement.getParameterFromJsonPath(jsonPath);
        }

        // If the JSON path starts with $, remove it
        if (jsonPath.startsWith("$")) {
            jsonPath = jsonPath.substring(1);
        }

        int start = jsonPath.indexOf("[");
        int end = jsonPath.indexOf("]");

        if (start >= 0 && end >= 0) {
            if ((jsonPath.charAt(start + 1) == '\'' || jsonPath.charAt(start + 1) == '"') &&
                    (jsonPath.charAt(end - 1) == '\'' || jsonPath.charAt(end - 1) == '"')) {
                String propertyName = jsonPath.substring(start + 2, end - 1);
                for (ParameterElement property : properties) {
                    if (property.getName().toString().equals(propertyName)) {
                        if (property instanceof ArrayParameter) {
                            return property.getParameterFromJsonPath(jsonPath.substring(end + 1));
                        } else {
                            return property.getParameterFromJsonPath(jsonPath);
                        }
                    }
                }
                return null;
            } else {
                // Missing quotes or double quotes
                return null;
            }
        } else {
            // Missing parenthesis
            return null;
        }
    }

}
