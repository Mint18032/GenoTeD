package io.testrest;

import io.testrest.datatype.parameter.NormalizedParameterName;
import io.testrest.dictionary.Dictionary;
import io.testrest.helper.ExtendedRandom;

public class Environment {


    private static Configuration configuration;
    private static Environment instance = null;
    private ExtendedRandom random;
    private Dictionary globalDictionary;

    public Environment() {
        configuration = new Configuration();
        NormalizedParameterName.setQualifiableNames(configuration.getQualifiableNames());
        this.globalDictionary = new Dictionary();
        this.random = new ExtendedRandom();
    }

    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
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
