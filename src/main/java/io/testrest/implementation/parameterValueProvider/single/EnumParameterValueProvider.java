package io.testrest.implementation.parameterValueProvider.single;

import io.testrest.Environment;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.helper.ExtendedRandom;
import io.testrest.implementation.parameterValueProvider.CountableParameterValueProvider;

import java.util.stream.Collectors;

public class EnumParameterValueProvider extends CountableParameterValueProvider {

    @Override
    public int countAvailableValuesFor(ParameterLeaf parameterLeaf) {
        if (!strict) {
            return parameterLeaf.getEnumValues().size();
        } else {
            return (int) parameterLeaf.getEnumValues().stream().filter(parameterLeaf::isValueCompliant).count();
        }
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        ExtendedRandom random = Environment.getInstance().getRandom();
        if (!strict) {
            return random.nextElement(parameterLeaf.getEnumValues()).orElse(null);
        } else {
            return random.nextElement(parameterLeaf.getEnumValues().stream().filter(parameterLeaf::isValueCompliant)
                    .collect(Collectors.toSet())).orElse(null);
        }
    }
}