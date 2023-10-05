package io.testrest.testing.parameterValueProvider.multi;

import io.testrest.Environment;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.helper.ExtendedRandom;
import io.testrest.testing.parameterValueProvider.CountableParameterValueProvider;
import io.testrest.testing.parameterValueProvider.ParameterValueProvider;
import io.testrest.testing.parameterValueProvider.single.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RandomProviderParameterValueProvider extends ParameterValueProvider {

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {

        ExtendedRandom random = Environment.getInstance().getRandom();

        Set<ParameterValueProvider> providers = new HashSet<>();

        // The random generator provider is always available
        providers.add(new RandomParameterValueProvider());

        // List of candidate providers, that will be used only if they have values available
        Set<CountableParameterValueProvider> candidateProviders = new HashSet<>();
        candidateProviders.add(new DefaultParameterValueProvider());
        candidateProviders.add(new EnumParameterValueProvider());
        candidateProviders.add(new ExamplesParameterValueProvider());
        candidateProviders.add(new NormalizedDictionaryParameterValueProvider());
        candidateProviders.add(new DictionaryParameterValueProvider());

        candidateProviders.forEach(p -> p.setStrict(this.strict));

        providers.addAll(candidateProviders.stream().filter(p -> p.countAvailableValuesFor(parameterLeaf) > 0)
                .collect(Collectors.toSet()));

        Optional<ParameterValueProvider> chosenProvider = random.nextElement(providers);
        return chosenProvider.map(parameterValueProvider ->
                parameterValueProvider.provideValueFor(parameterLeaf)).orElse(null);

    }
}

