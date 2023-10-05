package io.testrest.testing.parameterValueProvider.single;


import io.testrest.Environment;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.dictionary.Dictionary;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.helper.ExtendedRandom;
import io.testrest.testing.parameterValueProvider.CountableParameterValueProvider;

import java.util.Optional;
import java.util.stream.Collectors;

public class NormalizedDictionaryParameterValueProvider extends CountableParameterValueProvider {

    // Get values from global dictionary by default
    private Dictionary dictionary = Environment.getInstance().getGlobalDictionary();

    @Override
    public int countAvailableValuesFor(ParameterLeaf parameterLeaf) {
        if (!strict) {
            return dictionary.getEntriesByNormalizedParameterName(parameterLeaf.getNormalizedName(), parameterLeaf.getType()).size();
        } else {
            return (int) dictionary.getEntriesByNormalizedParameterName(parameterLeaf.getNormalizedName(), parameterLeaf.getType())
                    .stream().filter(e -> parameterLeaf.isValueCompliant(e.getValue())).count();
        }
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        ExtendedRandom random = Environment.getInstance().getRandom();
        Optional<DictionaryEntry> entry;
        if (!strict) {
            entry = random.nextElement(dictionary.getEntriesByNormalizedParameterName(parameterLeaf.getNormalizedName(),
                    parameterLeaf.getType()));
        } else {
            entry = random.nextElement(dictionary.getEntriesByNormalizedParameterName(parameterLeaf.getNormalizedName(),
                            parameterLeaf.getType()).stream().filter(e -> parameterLeaf.isValueCompliant(e.getValue()))
                    .collect(Collectors.toSet()));
        }
        return entry.get().getValue();
    }

    /**
     * Set the dictionary from which the provider picks the value.
     * @param dictionary the dictionary from which the provider picks the value.
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}

