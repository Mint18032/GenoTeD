package io.testrest.core.testGenerator;

public enum TraverseStrategy {
    ALL,
    UNCOVERED;

    public static TraverseStrategy getStrategy(String stringStrategy) {
        for (TraverseStrategy strategy : TraverseStrategy.values()) {
            if (strategy.name().equalsIgnoreCase(stringStrategy)) {
                return strategy;
            }
        }

        throw new IllegalArgumentException("Invalid value '" + stringStrategy + "' for fuzzing strategy.");
    }
}
