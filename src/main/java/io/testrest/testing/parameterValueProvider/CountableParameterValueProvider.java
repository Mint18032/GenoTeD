package io.testrest.testing.parameterValueProvider;

import io.testrest.datatype.parameter.ParameterLeaf;

/**
 * Parameter value providers that pick values from a deterministic source with a countable number of values.
 */
public abstract class CountableParameterValueProvider extends ParameterValueProvider {

    public abstract int countAvailableValuesFor(ParameterLeaf parameterLeaf);
}
