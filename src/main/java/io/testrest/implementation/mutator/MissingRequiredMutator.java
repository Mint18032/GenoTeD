package io.testrest.implementation.mutator;

import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.testing.Mutator;
import io.testrest.testing.TestInteraction;

import java.util.logging.Logger;

public class MissingRequiredMutator extends Mutator {

    private static final Logger logger = Logger.getLogger(MissingRequiredMutator.class.getName());

    /**
     * In order to be mutable, a parameter must be required
     * @param parameter the parameter to check
     * @return true if the parameter is mutable, false otherwise
     */
    @Override
    public boolean isParameterMutable(ParameterLeaf parameter) {
        return parameter.isRequired();
    }

    /**
     * Removes the value from the mandatory parameter. The request manager will
     */
    @Override
    public void mutate(DictionaryEntry entry, TestInteraction interaction) {
        if (isParameterMutable(entry.getSource())) {
            interaction.getRequestInputs().remove(entry);
        } else {
            logger.warning("Cannot apply mutation. This parameter is not mandatory.");
        }
    }
}
