package io.testrest.implementation.mutator;

import io.testrest.Environment;
import io.testrest.datatype.parameter.BooleanParameter;
import io.testrest.datatype.parameter.NumberParameter;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.StringParameter;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.helper.ExtendedRandom;
import io.testrest.testing.Mutator;
import io.testrest.testing.TestInteraction;
import io.testrest.testing.parameterValueProvider.ParameterValueProvider;
import io.testrest.testing.parameterValueProvider.single.RandomParameterValueProvider;

import java.util.logging.Logger;

public class WrongTypeMutator extends Mutator {

    private static final Logger logger = Logger.getLogger(WrongTypeMutator.class.getName());

    private final ParameterValueProvider valueProvider = new RandomParameterValueProvider();

    @Override
    public boolean isParameterMutable(ParameterLeaf parameter) {
        return parameter instanceof StringParameter || parameter instanceof NumberParameter ||
                parameter instanceof BooleanParameter;
    }

    @Override
    public void mutate(DictionaryEntry entry, TestInteraction interaction) {

        ExtendedRandom random = Environment.getInstance().getRandom();

        ParameterLeaf parameter = entry.getSource();
        ParameterLeaf mutatedParameter;

        if (parameter instanceof StringParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new NumberParameter(parameter);
            } else {
                mutatedParameter = new BooleanParameter(parameter);
            }

            interaction.getRequestInputs().remove(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else if (parameter instanceof NumberParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new StringParameter(parameter);
            } else {
                mutatedParameter = new BooleanParameter(parameter);
            }

            interaction.getRequestInputs().remove(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else if (parameter instanceof BooleanParameter) {
            if (random.nextBoolean()) {
                mutatedParameter = new StringParameter(parameter);
            } else {
                mutatedParameter = new NumberParameter(parameter);
            }

            interaction.getRequestInputs().remove(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(mutatedParameter, valueProvider.provideValueFor(mutatedParameter)));

        } else {
            logger.warning("Could not apply mutation. This parameter is not of a mutable type.");
        }
    }
}
