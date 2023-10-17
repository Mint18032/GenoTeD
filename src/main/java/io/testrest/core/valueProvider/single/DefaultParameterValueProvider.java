package io.testrest.core.valueProvider.single;

import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.core.valueProvider.CountableParameterValueProvider;

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
