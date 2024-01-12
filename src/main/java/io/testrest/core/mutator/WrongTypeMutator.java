package io.testrest.core.mutator;

import io.testrest.Main;
import io.testrest.datatype.parameter.BooleanParameter;
import io.testrest.datatype.parameter.NumberParameter;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.StringParameter;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.helper.ExtendedRandom;
import io.testrest.core.testing.TestInteraction;
import io.testrest.core.valueProvider.single.RandomParameterValueProvider;

import java.util.logging.Logger;

public class WrongTypeMutator extends Mutator {

    private static final Logger logger = Logger.getLogger(WrongTypeMutator.class.getName());

    private final RandomParameterValueProvider valueProvider = new RandomParameterValueProvider();

    @Override
    public boolean isParameterMutable(ParameterLeaf parameter) {
        return parameter instanceof StringParameter || parameter instanceof NumberParameter ||
                parameter instanceof BooleanParameter;
    }

    @Override
    public void mutate(DictionaryEntry entry, TestInteraction interaction) {
        System.out.println("Applying wrong type mutation.");

        ExtendedRandom random = Main.getEnvironment().getRandom();

        ParameterLeaf parameter = entry.getSource();
        ParameterLeaf mutatedParameter;

        if (parameter instanceof StringParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new NumberParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from String to Number.");
            } else {
                mutatedParameter = new BooleanParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from String to Boolean.");
            }

            interaction.removeInput(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else if (parameter instanceof NumberParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new StringParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from Number to String.");
            } else {
                mutatedParameter = new BooleanParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from Number to Boolean.");
            }

            interaction.removeInput(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else if (parameter instanceof BooleanParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new StringParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from Boolean to String.");
            } else {
                mutatedParameter = new NumberParameter(parameter);
                interaction.setMutateInfo("Wrong Type Mutation. Changed parameter '" + parameter.getName() + "' from Boolean to Number.");
            }

            interaction.removeInput(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else {
            logger.warning("Could not apply mutation. This parameter is not of a mutable type.");
        }
    }
}
