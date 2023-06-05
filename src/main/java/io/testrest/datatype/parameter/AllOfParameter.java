package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AllOfParameter extends CombinedSchemaParameter {

    private static final Logger logger = Logger.getLogger(AllOfParameter.class.getName());

    public AllOfParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);
    }

    public AllOfParameter(Parameter parameterMap, OperationNode operation, String name) {
        super(parameterMap, operation, name);
    }

    protected AllOfParameter(ParameterElement other) {
        super(other);
    }

    protected AllOfParameter(AllOfParameter other) {
        super(other);
    }

    protected AllOfParameter(AllOfParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);
    }

    @Override
    public ParameterElement merge() {
        ParameterElement merged = this.parametersSchemas.stream().findFirst().get();
        for (ParameterElement schema : this.parametersSchemas) {
            merged = merged.merge(schema);
        }

        return merged;
    }

    @Override
    protected String getKeyFiledName() {
        return "allOf";
    }

    @Override
    // TODO: implement
    public boolean isObjectTypeCompliant(Object o) {
        return false;
    }

    @Override
    public AllOfParameter deepClone() {
        return new AllOfParameter(this);
    }

    @Override
    public AllOfParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new AllOfParameter(this, operation, parent);
    }

}
