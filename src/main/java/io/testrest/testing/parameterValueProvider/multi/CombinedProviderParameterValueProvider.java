package io.testrest.testing.parameterValueProvider.multi;

import io.testrest.Environment;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.testing.parameterValueProvider.ParameterValueProvider;
import io.testrest.testing.parameterValueProvider.single.*;

import java.util.ArrayList;
import java.util.List;

public class CombinedProviderParameterValueProvider extends ParameterValueProvider {
    private Environment environment;
    protected NormalizedDictionaryParameterValueProvider normalizedDictionaryParameterValueProvider = new NormalizedDictionaryParameterValueProvider();
    protected RandomParameterValueProvider randomParameterValueProvider = new RandomParameterValueProvider();
    protected EnumParameterValueProvider enumParameterValueProvider = new EnumParameterValueProvider();
    protected DefaultParameterValueProvider defaultParameterValueProvider = new DefaultParameterValueProvider();
    protected ExamplesParameterValueProvider examplesParameterValueProvider = new ExamplesParameterValueProvider();

    public CombinedProviderParameterValueProvider() {
        environment = Environment.getInstance();
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        if (normalizedDictionaryParameterValueProvider.countAvailableValuesFor(parameterLeaf) > 0) {
            return normalizedDictionaryParameterValueProvider.provideValueFor(parameterLeaf);
        }
        if (parameterLeaf.isEnum()) {
            return enumParameterValueProvider.provideValueFor(parameterLeaf);
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
