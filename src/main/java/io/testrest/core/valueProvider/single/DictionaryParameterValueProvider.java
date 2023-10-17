package io.testrest.core.valueProvider.single;


import io.testrest.Environment;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.core.dictionary.Dictionary;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.helper.ExtendedRandom;
import io.testrest.core.valueProvider.CountableParameterValueProvider;

import java.util.Optional;
import java.util.stream.Collectors;

public class DictionaryParameterValueProvider extends CountableParameterValueProvider {

    // Get values from global dictionary by default
    private Dictionary dictionary = Environment.getInstance().getGlobalDictionary();

    @Override
    public int countAvailableValuesFor(ParameterLeaf parameterLeaf) {
        if (strict) {
            return dictionary.getEntriesByParameterName(parameterLeaf.getName(), parameterLeaf.getType()).size();
        } else {
            return (int) dictionary.getEntriesByParameterName(parameterLeaf.getName(), parameterLeaf.getType())
                    .stream().filter(e -> parameterLeaf.isValueCompliant(e.getValue())).count();
        }
    }

    @Override
    public Object provideValueFor(ParameterLeaf parameterLeaf) {
        ExtendedRandom random = Environment.getInstance().getRandom();
        Optional<DictionaryEntry> entry;
        if (!strict) {
            entry = random.nextElement(dictionary.getEntriesByParameterName(parameterLeaf.getName(), parameterLeaf.getType()));
        } else {
            entry = random.nextElement(dictionary.getEntriesByParameterName(parameterLeaf.getName(), parameterLeaf.getType())
                    .stream().filter(e -> parameterLeaf.isValueCompliant(e.getValue())).collect(Collectors.toSet()));
        }
        return entry.map(DictionaryEntry::getSource).orElse(null);
    }

    /**
     * Set the dictionary from which the provider picks the value.
     * @param dictionary the dictionary from which the provider picks the value.
     */
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}
