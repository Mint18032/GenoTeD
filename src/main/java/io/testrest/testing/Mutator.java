package io.testrest.testing;


import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.dictionary.DictionaryEntry;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Mutator {

    /**
     * Given a list of parameters, returns a sublist containing the mutable parameters only.
     * @param parameters the list of parameters to check
     * @return the list of mutable parameters
     */
    public List<ParameterLeaf> getMutableParameters(List<ParameterLeaf> parameters) {
        return parameters.stream().filter(this::isParameterMutable).collect(Collectors.toList());
    }

    /**
     * Check if a parameter is mutable.
     * @param parameter the parameter to check.
     * @return true if the passed parameter is mutable.
     */
    public abstract boolean isParameterMutable(ParameterLeaf parameter);

    /**
     * Apply the mutation to a parameter
     */
    public abstract void mutate(DictionaryEntry entry, TestInteraction interaction);

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
