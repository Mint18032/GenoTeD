package io.testrest.core.valueProvider;

public enum FuzzingStrategy {
    RANDOM,
    DICTIONARY_FIRST;

    public static FuzzingStrategy getStrategy(String stringStrategy) {
        for (FuzzingStrategy strategy : FuzzingStrategy.values()) {
            if (strategy.name().equalsIgnoreCase(stringStrategy)) {
                return strategy;
            }
        }

        throw new IllegalArgumentException("Invalid value '" + stringStrategy + "' for fuzzing strategy.");
    }
}
