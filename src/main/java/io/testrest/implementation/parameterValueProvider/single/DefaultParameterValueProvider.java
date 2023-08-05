package io.testrest.implementation.parameterValueProvider.single;

import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.implementation.parameterValueProvider.CountableParameterValueProvider;

public class DefaultParameterValueProvider extends CountableParameterValueProvider {

    @Override
    public int countAvailableValuesFor(ParameterLeaf parameterLeaf) {
        if (parameterLeaf.getDefaultValue() != null) {
            if (!strict || parameterLeaf.isValueCompliant(parameterLeaf.getDefaultValue())) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        return parameterLeaf.getDefaultValue();
    }
}
