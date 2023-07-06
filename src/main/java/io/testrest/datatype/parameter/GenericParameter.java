package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

public class GenericParameter extends ParameterLeaf {

    private static final Logger logger = Logger.getLogger(GenericParameter.class.getName());


    public GenericParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);
    }

    public GenericParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation) {
        super(parent, parameterMap, operation);
    }

    protected GenericParameter(Parameter other) {
        super(other);
    }

    protected GenericParameter(ParameterLeaf other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);
    }

    @Override
    public ParameterElement merge(ParameterElement other) {
        logger.warning("Cannot merge " + GenericParameter.class + " instances.");
        return this;
    }

    @Override
    public boolean isValueCompliant(Object value) {
        return true;
    }

    @Override
    public boolean isObjectTypeCompliant(Object o) {
        return true;
    }

    @Override
    public String getJSONString() {
        return getJSONHeading() + getConcreteValue().toString();
    }

    @Override
    public ParameterElement deepClone() {
        return this;
    }

    @Override
    public ParameterElement deepClone(OperationNode operation, ParameterElement parent) {
        return new GenericParameter(this, operation, parent);
    }

    /**
     * No arrays are available at this level. No underlying parameters are available in leaves.
     * @return an empty list
     */
    @Override
    public Collection<ParameterArray> getArrays() {
        return new LinkedList<>();
    }
}

