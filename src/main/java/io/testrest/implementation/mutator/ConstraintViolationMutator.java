package io.testrest.implementation.mutator;

import io.testrest.Environment;
import io.testrest.datatype.parameter.NumberParameter;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.StringParameter;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.helper.ExtendedRandom;
import io.testrest.testing.Mutator;
import io.testrest.testing.TestInteraction;
import io.testrest.testing.parameterValueProvider.ParameterValueProvider;
import io.testrest.testing.parameterValueProvider.single.RandomParameterValueProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ConstraintViolationMutator extends Mutator {

    private static final Logger logger = Logger.getLogger(ConstraintViolationMutator.class.getName());
    private static final ParameterValueProvider valueProvider = new RandomParameterValueProvider();

    @Override
    public boolean isParameterMutable(ParameterLeaf parameter) {

        // Check if parameter is an enum
        if (parameter.isEnum() && parameter.getEnumValues().size() > 0) {
            return true;
        }

        // Check if string parameters have length constraints
        else if (parameter instanceof StringParameter) {
            return ((StringParameter) parameter).getMaxLength() != null ||
                    (((StringParameter) parameter).getMinLength() != null &&
                            ((StringParameter) parameter).getMinLength() > 0);
        }

        // Check if number parameters have value constraints
        else if (parameter instanceof NumberParameter) {
            return ((NumberParameter) parameter).getMinimum() != null ||
                    ((NumberParameter) parameter).getMaximum() != null;
        }

        // Others parameters are not mutable
        return false;
    }

    @Override
    public void mutate(DictionaryEntry entry, TestInteraction interaction) {
        System.out.println("Applying constraint violation mutation.");

        ParameterLeaf parameter = entry.getSource();
        DictionaryEntry newEntry = new DictionaryEntry(entry.getSource(), entry.getValue());

        if (isParameterMutable(parameter)) {
            if (parameter.isEnum() && parameter.getEnumValues().size() > 0) {
                Object mutateValue = mutateEnum(parameter);
                newEntry.setValue(mutateValue.toString());
                interaction.setMutateInfo("Constraint Violation Mutation. Provided random value to enum parameter " + parameter.getName());
            } else if (parameter instanceof StringParameter) {
                String mutateValue = mutateLength((StringParameter) parameter);
                newEntry.setValue(mutateValue);
                interaction.setMutateInfo("Constraint Violation Mutation. Violated length constraint of parameter " + parameter.getName());
            } else if (parameter instanceof NumberParameter) {
                Object mutateValue = mutateNumber((NumberParameter) parameter);
                if (mutateValue != null) {
                    newEntry.setValue((Double) mutateValue);
                    interaction.setMutateInfo("Constraint Violation Mutation. Violated range constraint of parameter " + parameter.getName());
                }
            }

            interaction.removeInput(entry);
            interaction.getRequestInputs().add(newEntry);

        } else {
            logger.warning("The provided parameter cannot be mutated because it does not provide constraints to violate.");
        }

    }

    /**
     * Assigns to the parameter a value outside the enum constraints
     * @param parameter the parameter to mutate
     */
    private Object mutateEnum(ParameterLeaf parameter) {
        // Set a random value to the parameter (it is very unlikely that the generated value belongs to the enum values
        // FIXME: check that the generated value does not belong to the enum values
        return valueProvider.provideValueFor(parameter);

        //TODO: remove: it is replaced by RandomValueProvider
        //parameter.setValue(parameter.generateCompliantValue());
    }

    /**
     * String length is changed to violate length constraints
     * @param parameter the parameter to mutate
     */
    private String mutateLength(StringParameter parameter) {

        ExtendedRandom random = Environment.getInstance().getRandom();

        // Save to this list the possible lengths of the mutated string
        List<Integer> lengths = new ArrayList<>();
        if (parameter.getMinLength() != null && parameter.getMinLength() > 1) {
            lengths.add(random.nextLength(0, parameter.getMinLength()));
        }
        if (parameter.getMaxLength() != null && parameter.getMaxLength() < Integer.MAX_VALUE - 1) {
            lengths.add(random.nextLength(parameter.getMaxLength() + 1, Integer.MAX_VALUE));
        }

        // Choose a random length
        Optional<Integer> chosenLength = random.nextElement(lengths);

        // If the current value is longer, just cut the string
        if (chosenLength.isPresent() && ((String) parameter.getValue()).length() > chosenLength.get()) {
            return ((String) parameter.getValue()).substring(0, chosenLength.get());
        }

        // If the current value is shorter, add random characters
        else if (chosenLength.isPresent() && ((String) parameter.getValue()).length() < chosenLength.get()) {
            return (parameter.getValue() +
                    random.nextRandomString(chosenLength.get() - ((String) parameter.getValue()).length()));
        }

        return null;
    }

    /**
     * Number value is changed to violate value constraints
     * FIXME: generate number (superclass) instead of double
     * @param parameter the parameter to mutate
     */
    private Object mutateNumber(NumberParameter parameter) {

        ExtendedRandom random = Environment.getInstance().getRandom();

        List<Double> values = new ArrayList<>();
        if (parameter.getMinimum() != null && parameter.getMinimum() > Double.MIN_VALUE) {
            values.add(random.nextDouble(Double.MIN_VALUE, parameter.getMinimum()));
        }
        if (parameter.getMaximum() != null && parameter.getMaximum() < Double.MAX_VALUE) {
            values.add(random.nextDouble(parameter.getMaximum(), Double.MAX_VALUE));
        }

        Optional<Double> value = random.nextElement(values);

        return value.orElse(null);
    }
 }
