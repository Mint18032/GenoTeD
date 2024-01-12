package io.testrest.core.valueProvider.single;

import io.testrest.Main;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.helper.ExtendedRandom;
import io.testrest.core.valueProvider.CountableParameterValueProvider;

import java.util.stream.Collectors;

public class ExamplesParameterValueProvider extends CountableParameterValueProvider {

    @Override
    public int countAvailableValuesFor(ParameterLeaf parameterLeaf) {
        if (!strict) {
            return parameterLeaf.getExamples().size();
        } else {
            return (int) parameterLeaf.getExamples().stream().filter(parameterLeaf::isValueCompliant).count();
        }
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        ExtendedRandom random = Main.getEnvironment().getRandom();
        if (!strict) {
            return random.nextElement(parameterLeaf.getExamples()).orElse(null);
        } else {
            return random.nextElement(parameterLeaf.getExamples().stream().filter(parameterLeaf::isValueCompliant)
                    .collect(Collectors.toSet())).orElse(null);
        }
    }
}