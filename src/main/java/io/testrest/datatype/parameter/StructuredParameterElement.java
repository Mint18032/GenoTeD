package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.helper.ObjectHelper;
import io.testrest.parser.EditReadOnlyOperationException;
import io.testrest.datatype.graph.OperationNode;

import java.util.HashSet;
import java.util.logging.Logger;

public abstract class StructuredParameterElement extends ParameterElement {
    // By default, remove a structured parameter instead keeping it empty when representing it
    private boolean keepIfEmpty = false;

    public StructuredParameterElement(ParameterElement parent, Parameter parameter, OperationNode operation, String name) {
        super(parent, parameter, operation, name);
    }

    public StructuredParameterElement(ParameterElement parent, Parameter parameter, OperationNode operation) {
        this(parent, parameter, operation, null);
    }

    protected StructuredParameterElement(Parameter other) {
        super(other);
        this.keepIfEmpty = other.getAllowEmptyValue();
    }

    protected StructuredParameterElement(StructuredParameterElement other, OperationNode operation, ParameterElement parent) {
        super(operation, parent);
        name = other.getName();
        schemaName = other.getSchemaName();
        required = other.isRequired();
        type = other.getType(); // Amedeo did: ParameterType.getTypeFromString(other.type.name());
        format = other.getFormat();
        setLocation(other.getLocation());
        setStyle(other.getStyle());
        setExplode(other.isExplode());
        setOperation(other.getOperation());

        setDescription(other.getDescription());

        defaultValue = ObjectHelper.deepCloneObject(other.getDefaultValue());
        enumValues = new HashSet<>(ObjectHelper.deepCloneObject(other.getEnumValues()));
        examples = new HashSet<>(ObjectHelper.deepCloneObject(other.getExamples()));
    }

    public StructuredParameterElement(OperationNode operation, ParameterElement parent) {
        super(operation, parent);
    }

    public StructuredParameterElement(Parameter p, OperationNode operation) {
        super(p, operation);
    }

    @Override
    public abstract StructuredParameterElement deepClone();

    @Override
    public abstract StructuredParameterElement deepClone(OperationNode operation, ParameterElement parent);

    /**
     * Function that returns whether the structured parameter is empty or not.
     * @return Boolean value representing whether the structured parameter is empty
     */
    public abstract boolean isEmpty();

    /**
     * Function to retrieve the boolean value that is used to keep track if the structured parameter should be kept
     * if empty or not.
     * @return Boolean value representing whether the structured parameter should be represented when empty
     */
    public final boolean isKeepIfEmpty() {
        return keepIfEmpty;
    }

    public final void setKeepIfEmpty(boolean keepIfEmpty) {
        if (getOperation().isReadOnly()) {
            throw new EditReadOnlyOperationException(getOperation());
        }
        this.keepIfEmpty = keepIfEmpty;
    }

    @Override
    public String getJsonPath() {
        if (getParent() == null) {
            return "$";
        }

        else if (getParent() instanceof ArrayParameter) {

            // If this is the referenceElement of the array, return index = -1
            if (this == ((ArrayParameter) getParent()).getReferenceElement()) {
                return getParent().getJsonPath() + "[-1]";
            }

            // If this is an element of the array, return its index
            else if (((ArrayParameter) getParent()).getElements().contains(this)) {
                return getParent().getJsonPath() + "[" + ((ArrayParameter) getParent()).getElements().indexOf(this) + "]";
            }

            // If this is not contained in the array, return null
            else {
                return null;
            }
        }

        else {
            return getParent().getJsonPath() + "['" + this.getName() + "']";
        }
    }

    // TODO: remove
    //public abstract ParameterElement getElementByJsonPath(String jsonPath);
/*
    @Override
    public boolean remove() {

        // If the leaf has no parent (it is a root), then remove it from the operation
        if (getParent() == null) {
            switch (getLocation()) {
                case REQUEST_BODY:
                    if (this == getOperation().getRequestBody()) {
                        getOperation().setRequestBody(null);
                        return true;
                    }
                    break;
                case RESPONSE_BODY:
                    if (this == getOperation().getResponseBody()) {
                        getOperation().setResponseBody(null);
                        return true;
                    }
                    break;
                case QUERY:
                    return getOperation().getQueryParameters().remove(this);
                case PATH:
                    return getOperation().getPathParameters().remove(this);
                case HEADER:
                    return getOperation().getHeaderParameters().remove(this);
                case COOKIE:
                    return getOperation().getCookieParameters().remove(this);
            }
        }

        // If the leaf is contained in a parent element (array or object), remove it from the parent
        else {
            if (getParent() instanceof ArrayParameter) {
                return ((ArrayParameter) getParent()).getElements().remove(this);
            } else if (getParent() instanceof ObjectParameter) {
                return ((ObjectParameter) getParent()).getProperties().remove(this);
            }
        }

        return false;
    }
*/

}

