package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

import java.util.Map;
import java.util.logging.Logger;

public class AnyOfParameter extends CombinedSchemaParameter {

    private static final Logger logger = Logger.getLogger(AnyOfParameter.class.getName());

    public AnyOfParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);
    }

    public AnyOfParameter(Parameter parameterMap, OperationNode operation, String name) {
        super(parameterMap, operation, name);
    }

    protected AnyOfParameter(Parameter other) {
        super(other);
    }

    protected AnyOfParameter(AnyOfParameter other) {
        super(other);
    }

    protected AnyOfParameter(AnyOfParameter other, OperationNode operation, ParameterElement parent) {
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
        return "anyOf";
    }

    @Override
    // TODO: implement
    public boolean isObjectTypeCompliant(Object o) {
        return false;
    }

    @Override
    public AnyOfParameter deepClone() {
        return new AnyOfParameter(this);
    }

    @Override
    public AnyOfParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new AnyOfParameter(this, operation, parent);
    }
}
