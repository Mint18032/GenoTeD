package io.testrest.datatype.parameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.parser.EditReadOnlyOperationException;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.parser.UnsupportedSpecificationFeature;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ArrayParameter extends StructuredParameterElement {

    private ParameterElement referenceElement;
    private List<ParameterElement> elements;

    private Integer minItems;
    private Integer maxItems;
    private boolean uniqueItems;

    private static final Logger logger = Logger.getLogger(ArrayParameter.class.getName());

    @SuppressWarnings("unchecked")
    public ArrayParameter(ParameterElement parent, Parameter parameter, OperationNode operation, String name) {
        super(parent, parameter, operation, name);
        elements = new LinkedList<>();

        Schema schema = parameter.getSchema();
        minItems = schema.getMinItems();
        maxItems = schema.getMaxItems();
        uniqueItems = schema.getUniqueItems();

        setNormalizedName(NormalizedParameterName.computeParameterNormalizedName(this));

        // Catch the difference between leaf and structured parameters
        Schema targetSource = schema.getItems();

        if (targetSource != null) {
            Schema items = targetSource.getItems();
            try {
                Parameter param = parameter;
                param.setIn(getLocation().toString());
                param.setSchema(items);
                referenceElement = ParameterFactory.getParameterElement(this, param, operation, getNormalizedName().toString());

                // Propagate example values to children
                for (Object example : super.examples) {
                    List<Object> exampleItems = (List<Object>) example;
                    for (Object item : exampleItems) {
                        referenceElement.addExample(item);
                    }
                }

            } catch (UnsupportedSpecificationFeature e) {
                throw new ParameterCreationException("Unable to parse reference element for property \"" + name +
                        "\" (normalized as: \"" + name + "\") " +
                        "due to an unsupported feature in OpenAPI specification.");

            }
        }
    }

    public ArrayParameter(Parameter other) {
        super(other);
// TODO: ref elements of array
//        referenceElement = other.referenceElement.deepClone();
        elements = new LinkedList<>();
//        other.elements.forEach(e -> elements.add(e.deepClone()));
        minItems = other.getSchema().getMinItems();
        maxItems = other.getSchema().getMaxItems();
        uniqueItems = other.getSchema().getUniqueItems();
    }

    public ArrayParameter(Parameter other, OperationNode operation) {
        super(other, operation);
// TODO: ref elements of array
//        referenceElement = other.referenceElement.deepClone();
        elements = new LinkedList<>();
//        other.elements.forEach(e -> elements.add(e.deepClone()));
        minItems = other.getSchema().getMinItems();
        maxItems = other.getSchema().getMaxItems();
        uniqueItems = other.getSchema().getUniqueItems();
    }

    private ArrayParameter(ArrayParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);

        referenceElement = other.referenceElement != null ? other.referenceElement.deepClone(operation, this) : null;
        elements = new LinkedList<>();
        other.elements.forEach(e -> elements.add(e.deepClone(operation, this)));
        minItems = other.minItems;
        maxItems = other.maxItems;
        uniqueItems = other.uniqueItems;
    }

    public ArrayParameter(JsonArray jsonArray, OperationNode operation, ParameterElement parent, String name) {
        super(operation, parent);

        this.elements = new LinkedList<>();
        this.name = new ParameterName(Objects.requireNonNullElse(name, ""));
        this.normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
        this.type = ParameterType.ARRAY;

        for (JsonElement jsonElement : jsonArray) {
            ParameterElement p =
                    ParameterFactory.getParameterElement(this, jsonElement, operation, this.name.toString());
            if (p != null) {
                elements.add(p);
            }
        }
    }

    public ArrayParameter merge(ParameterElement other) {
        if (!(other instanceof ArrayParameter)) {
            throw new IllegalArgumentException("Cannot merge a " + this.getClass() + " instance with a "
                    + other.getClass() + " one.");
        }

        ArrayParameter stringParameter = (ArrayParameter) other;
        ArrayParameter merged = this;
        merged.referenceElement = this.referenceElement.merge(stringParameter.referenceElement);

        return merged;
    }

    public Integer getMinItems() {
        return minItems;
    }

    public void setMinItems(Integer minItems) {
        this.minItems = minItems;
    }

    public Integer getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public int indexOf(ParameterElement element) {
        return elements.indexOf(element);
    }

    @Override
    public boolean isObjectTypeCompliant(Object o) {
        if (o == null) {
            return false;
        }
        return List.class.isAssignableFrom(o.getClass());
    }

    public ParameterElement getReferenceElement() {
        return this.referenceElement;
    }

    public void setReferenceElement(ParameterElement referenceElement) {
        this.referenceElement = referenceElement;
    }

    @Override
    public Collection<ParameterLeaf> getLeaves() {
        Collection<ParameterLeaf> leaves = new LinkedList<>();

        for (ParameterElement element : elements) {
            leaves.addAll(element.getLeaves());
        }

        return leaves;
    }

    @Override
    public Collection<ParameterLeaf> getReferenceLeaves() {
        return new LinkedList<>(referenceElement.getReferenceLeaves());
    }

    /**
     * A ArrayParameter is considered empty when it has no elements.
     * @return true if the instance has no elements; false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    @Override
    public String getJSONString(Object value) {
        StringBuilder stringBuilder = new StringBuilder(getJSONHeading() + "[");
        elements.forEach(e -> stringBuilder.append(e.getJSONString(value)).append(", "));
        int index = stringBuilder.lastIndexOf(",");
        return stringBuilder.substring(0, index > 0 ? index : stringBuilder.length()) + "]";
    }

    public List<ParameterElement> getElements() {
        if (getOperation().isReadOnly()) {
            return Collections.unmodifiableList(elements);
        }
        return elements;
    }

    /**
     * Adds the given element to the instance element list. Can throw a EditReadOnlyOperationException if the
     * instance is in read-only mode.
     * @param element The ParameterElement to be added to the elements list
     */
    public void addElement(ParameterElement element) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }
        element.setParent(this);
        elements.add(element);
    }

    /**
     * Adds n copies of the reference element to the array. Can throw a EditReadOnlyOperationException if the instance
     * is in read-only mode.
     * @param n Number of copies to be put into the array
     */
    public void addReferenceElements(int n) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }
        for (int i = 0; i < n; i++) {
            addElement(referenceElement.deepClone());
        }
    }

    /**
     * Removes all the elements in the elements list of the instance
     */
    public void clearElements() {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }
        this.elements.clear();
    }

    @Override
    public boolean addExample(Object o) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }

        if (!super.addExample(o)) {
            return false;
        }

        List<Object> exampleItems = (List<Object>) o;
        // Propagate example values to children
        exampleItems.forEach(item -> referenceElement.addExample(item));

        return true;
    }

    public String getValueAsFormattedString (ParameterStyle style, boolean explode, Object value) {
        logger.warning("Format for deep nested arrays is not defined in the reference RFC. Use this method only for " +
                "RFC defined behaviors.");
        StringBuilder stringBuilder = new StringBuilder();
        switch (style) {
            case MATRIX:
                stringBuilder.append(";");
                if (explode) {
                    elements.forEach(e -> {
                        stringBuilder.append(getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(";");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == ';') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                } else {
                    stringBuilder.append(this.getName().toString());
                    stringBuilder.append("=");
                    elements.forEach(e -> {
                        stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                }
                return stringBuilder.toString();

            case LABEL:
                elements.forEach(e -> {
                    stringBuilder.append(".");
                    stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                });
                return stringBuilder.toString();

            case FORM:
                if (explode) {
                    elements.forEach(e -> {
                        stringBuilder.append(this.getName().toString());
                        stringBuilder.append("=");
                        stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append("&");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == '&') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                } else {
                    stringBuilder.append(this.getName().toString());
                    stringBuilder.append("=");
                    elements.forEach(e -> {
                        stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                        stringBuilder.append(",");
                    });
                    if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                }
                return stringBuilder.toString();

            case SIMPLE:
                elements.forEach(e -> {
                    stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append(",");
                });
                if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            case SPACE_DELIMITED:
                elements.forEach(e -> {
                    stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append("%20");
                });
                if (stringBuilder.length() > 0) {
                    return stringBuilder.substring(0, stringBuilder.length() - 3);
                }
                return stringBuilder.toString();

            case PIPE_DELIMITED:
                elements.forEach(e -> {
                    stringBuilder.append(e.getValueAsFormattedString(ParameterStyle.SIMPLE));
                    stringBuilder.append("|");
                });
                if (stringBuilder.charAt(stringBuilder.length() - 1) == '|') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return stringBuilder.toString();

            case DEEP_OBJECT:
            default:
                logger.warning(getName() + ": Style not consistent with parameter type. Returning 'simple' style.");
                return this.getValueAsFormattedString(ParameterStyle.SIMPLE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) return false;
        ArrayParameter that = (ArrayParameter) o;
        return Objects.equals(referenceElement, that.referenceElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), referenceElement);
    }

    @Override
    public ArrayParameter deepClone() {
        return this;
    }

    @Override
    public ArrayParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new ArrayParameter(this, operation, parent);
    }

    @Override
    public String toString() {
        if (elements != null && elements.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getName()).append(": [");
            elements.forEach(e -> stringBuilder.append(e.toString()).append(", "));
            stringBuilder.append("]");
            return stringBuilder.toString();
        } else if (referenceElement != null) {
            return getName() + ": [" + referenceElement.toString() + "]";
        } else {
            return getName() + ": []";
        }
    }

    /**
     * Returns itself, plus the arrays contained in its elements.
     * @return itself, plus the arrays contained in its elements.
     */
    @Override
    public Collection<ArrayParameter> getArrays() {
        Collection<ArrayParameter> arrays = new LinkedList<>();

        // Add this array
        arrays.add(this);

        // For each parameter contained in the array, add their arrays.
        for (ParameterElement element : elements) {
            arrays.addAll(element.getArrays());
        }

        return arrays;
    }

    @Override
    public Collection<ObjectParameter> getObjects() {
        Collection<ObjectParameter> objects = new LinkedList<>();
        elements.forEach(ParameterElement::getObjects);
        return objects;
    }

    @Override
    public Collection<ObjectParameter> getReferenceObjects() {
        return referenceElement.getReferenceObjects();
    }

    @Override
    public Collection<ParameterElement> getAllParameters() {
        HashSet<ParameterElement> parameters = new HashSet<>();
        parameters.add(this);
        if (referenceElement != null) {
            parameters.addAll(referenceElement.getAllParameters());
        }
        elements.forEach(e -> parameters.addAll(e.getAllParameters()));
        return parameters;
    }

    @Override
    public Collection<CombinedSchemaParameter> getCombinedSchemas() {
        Collection<CombinedSchemaParameter> combinedSchemas = new LinkedList<>();

        for (ParameterElement element : elements) {
            combinedSchemas.addAll(element.getCombinedSchemas());
        }

        return combinedSchemas;
    }

    @Override
    public boolean isSet() {
        return elements.size() > 0;
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
            int elementIndex = Integer.parseInt(jsonPath.substring(start + 1, end));
            // Index -1 stands for reference element
            if (elementIndex == -1) {
                return referenceElement.getParameterFromJsonPath(jsonPath.substring(end + 1));
            } else if (elementIndex >= 0 && elementIndex < elements.size()) {
                return elements.get(elementIndex).getParameterFromJsonPath(jsonPath.substring(end + 1));
            } else {
                // Index is invalid
                return null;
            }
        } else {
            // Missing parenthesis
            return null;
        }
    }
}

