package io.testrest;

import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.datatype.parameter.NormalizedParameterName;
import io.testrest.core.dictionary.Dictionary;
import io.testrest.helper.ExtendedRandom;

import java.util.ArrayList;
import java.util.Map;

public class Environment {

    private static Configuration configuration;
    private static Environment instance = null;
    private ExtendedRandom random;
    private Dictionary globalDictionary;

    public Environment(String configPath) {
        configuration = new Configuration(configPath);
        NormalizedParameterName.setQualifiableNames(configuration.getQualifiableNames());
        this.globalDictionary = new Dictionary();
        this.random = new ExtendedRandom();

        // Add values from default dictionary to global dictionary
        if (configuration.getConfigMap().containsKey("default_dictionary")) {
            if (configuration.getConfigMap().get("default_dictionary") instanceof Map) {
                Map default_dictionary = (Map) configuration.getConfigMap().get("default_dictionary");
                for (Object key : default_dictionary.keySet()) {
                    if (default_dictionary.get(key) instanceof ArrayList)
                        for (Object value : (ArrayList) default_dictionary.get(key)) {
                            globalDictionary.addEntry(new DictionaryEntry(key.toString(), value.toString()));
                        }
                }
            }
        }

        instance = this;
    }

    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment(Configuration.getConfigPath());
        }
        return instance;
    }

    public Dictionary getGlobalDictionary() {
        return globalDictionary;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(Configuration configuration) {
        Environment.configuration = configuration;
    }

    public static void setInstance(Environment instance) {
        Environment.instance = instance;
    }

    public ExtendedRandom getRandom() {
        return random;
    }

    public void setRandom(ExtendedRandom random) {
        this.random = random;
    }

    public void setGlobalDictionary(Dictionary globalDictionary) {
        this.globalDictionary = globalDictionary;
    }
}
