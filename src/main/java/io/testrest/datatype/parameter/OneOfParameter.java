package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

import java.util.Map;
import java.util.logging.Logger;

public class OneOfParameter extends CombinedSchemaParameter {

    private static final Logger logger = Logger.getLogger(OneOfParameter.class.getName());

    public OneOfParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);
    }

    public OneOfParameter(Parameter parameterMap, OperationNode operation, String name) {
        super(parameterMap, operation, name);
    }

    protected OneOfParameter(Parameter other) {
        super(other);
    }

    protected OneOfParameter(OneOfParameter other) {
        super(other);
    }

    protected OneOfParameter(OneOfParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);
    }

    @Override
    public ParameterElement merge() {
        // TODO: pick randomly if needed
        ParameterElement merged = this.parametersSchemas.stream().findFirst().get();

        return merged;
    }

    @Override
    protected String getKeyFiledName() {
        return "oneOf";
    }

    @Override
    // TODO: implement
    public boolean isObjectTypeCompliant(Object o) {
        return false;
    }

    @Override
    public OneOfParameter deepClone() {
        return new OneOfParameter(this);
    }

    @Override
    public OneOfParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new OneOfParameter(this, operation, parent);
    }
}
