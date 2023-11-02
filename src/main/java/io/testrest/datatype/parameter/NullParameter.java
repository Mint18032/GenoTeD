package io.testrest.datatype.parameter;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

public class NullParameter extends ParameterLeaf {

    /*
    The value of the instances of this class is always the string "null". The use of the string instead of the
    null value from Java has two main reasons:
    - avoid matching a NullParameter instance as a non-initialized ParameterLeaf
    - avoid the crash in many methods and using okhttp caused by NullPointerExceptions
     */

    public NullParameter(Parameter other) {
        super(other);
    }

    public NullParameter(NullParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);
    }

    public NullParameter(JsonElement jsonElement, OperationNode operation, ParameterElement parent, String name) {
        super(operation, parent);

        this.name = new ParameterName(Objects.requireNonNullElse(name, ""));
        this.normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
        this.type = ParameterType.UNKNOWN;
    }

    public NullParameter merge(ParameterElement other) {
        // No additional behavior/constraints in null parameter
        return this;
    }

    @Override
    public boolean isObjectTypeCompliant(Object o) {
        return o == null;
    }

    @Override
    public boolean isValueCompliant(Object value) {
        return value == null || value.toString().equals("null") || value instanceof NullParameter;
    }

    /**
     * No arrays are available at this level. No underlying parameters are available in leaves.
     * @return an empty list
     */
    @Override
    public Collection<ArrayParameter> getArrays() {
        return new LinkedList<>();
    }

    @Override
    public String getJSONString(Object value) {
        return getJSONHeading() + "null";
    }

    @Override
    public NullParameter deepClone() {
        return this;
    }

    @Override
    public NullParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new NullParameter(this, operation, parent);
    }

}

