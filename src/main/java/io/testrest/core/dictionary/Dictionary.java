package io.testrest.core.dictionary;

import io.testrest.datatype.parameter.NormalizedParameterName;
import io.testrest.datatype.parameter.ParameterName;
import io.testrest.datatype.parameter.ParameterType;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Dictionary that stores parameter values to be reused. Values are loaded from a default dictionary (file) and taken
 * from output data observed during testing. Each value is associated to a source that tells where that particular
 * value was observed
 */
public class Dictionary {

    private final List<DictionaryEntry> dictionary = new LinkedList<>();

    /**
     * Add an entry to the dictionary. If a similar entry already exists, it just updates the discovery time.
     * @param dictionaryEntry the entry to add.
     */
    public void addEntry(DictionaryEntry dictionaryEntry) {

        // Get list of similar entries
        List<DictionaryEntry> matchingEntries = dictionary.stream()
                .filter(entry -> entry.getParameterName().equals(dictionaryEntry.getParameterName()) &&
                        entry.getNormalizedParameterName().equals(dictionaryEntry.getNormalizedParameterName()) &&
                        entry.getValue().equals(dictionaryEntry.getValue()) &&
                        entry.getParameterType().equals(dictionaryEntry.getParameterType()))
                .collect(Collectors.toList());

        // If there are no similar entries, add entry to dictionary
        if (matchingEntries.size() == 0) {
            dictionary.add(dictionaryEntry);
        }

        // Otherwise, update similar entry with new discovery time
        else {
            matchingEntries.get(0).setDiscoveryTime(dictionaryEntry.getDiscoveryTime());
            matchingEntries.get(0).setSource(dictionaryEntry.getSource());
        }
    }

    public List<DictionaryEntry> getEntriesByNormalizedParameterName(NormalizedParameterName normalizedParameterName,
                                                                     ParameterType parameterType) {
        return dictionary.stream().filter(e -> e.getNormalizedParameterName().equals(normalizedParameterName) && (
                e.getParameterType() == null || e.getParameterType().equals(parameterType))).collect(Collectors.toList());
    }

    public List<DictionaryEntry> getEntriesByParameterName(ParameterName parameterName, ParameterType parameterType) {
        return dictionary.stream().filter(e -> e.getParameterName().equals(parameterName) &&
                e.getParameterType().equals(parameterType)).collect(Collectors.toList());
    }

    public List<DictionaryEntry> getEntriesByValueLength(int length) {
        return dictionary.stream().filter(e -> e.getValue().toString().length() == length).collect(Collectors.toList());
    }
}
