package io.testrest.core.valueProvider.multi;

import io.testrest.Environment;
import io.testrest.Main;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.core.valueProvider.single.*;
import io.testrest.core.valueProvider.ParameterValueProvider;

import java.util.ArrayList;
import java.util.List;

public class CombinedProviderParameterValueProvider extends ParameterValueProvider {
    private Environment environment;
    protected NormalizedDictionaryParameterValueProvider normalizedDictionaryParameterValueProvider = new NormalizedDictionaryParameterValueProvider();
    protected RandomParameterValueProvider randomParameterValueProvider = new RandomParameterValueProvider();
    protected EnumParameterValueProvider enumParameterValueProvider = new EnumParameterValueProvider();
    protected RegexParameterValueProvider regexParameterValueProvider = new RegexParameterValueProvider();
    protected DefaultParameterValueProvider defaultParameterValueProvider = new DefaultParameterValueProvider();
    protected ExamplesParameterValueProvider examplesParameterValueProvider = new ExamplesParameterValueProvider();

    public CombinedProviderParameterValueProvider() {
        environment = Main.getEnvironment();
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        if (normalizedDictionaryParameterValueProvider.countAvailableValuesFor(parameterLeaf) > 0) {
            return normalizedDictionaryParameterValueProvider.provideValueFor(parameterLeaf);
        }
        if (parameterLeaf.isEnum()) {
            return enumParameterValueProvider.provideValueFor(parameterLeaf);
        }

        Object valueFromRegex = regexParameterValueProvider.provideValueFor(parameterLeaf);
        if (valueFromRegex != null) {
            System.out.println("REGEX VALUE: " + valueFromRegex);
            return valueFromRegex;
        }

        List<Object> possibleValues = new ArrayList<>();
        if (parameterLeaf.getExamples() != null) {
            possibleValues.add(examplesParameterValueProvider.provideValueFor(parameterLeaf));
        }
        if (parameterLeaf.getDefaultValue() != null) {
            possibleValues.add(defaultParameterValueProvider.provideValueFor(parameterLeaf));
        }
        possibleValues.add(randomParameterValueProvider.provideValueFor(parameterLeaf));
        System.out.println(parameterLeaf.getName());
        System.out.println(possibleValues);

        return possibleValues.get(getEnvironment().getRandom().nextInt(0, possibleValues.size()));
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
