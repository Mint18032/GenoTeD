package io.testrest.core.mutator;

import com.github.curiousoddman.rgxgen.RgxGen;
import io.testrest.Environment;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.core.testing.TestInteraction;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.StringParameter;
import io.testrest.helper.ExtendedRandom;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UnmatchedRegexMutator extends Mutator {

    private static final Logger logger = Logger.getLogger(UnmatchedRegexMutator.class.getName());

    @Override
    public boolean isParameterMutable(ParameterLeaf parameter) {
        return parameter instanceof StringParameter && ((StringParameter) parameter).getPattern() != null;
    }

    @Override
    public void mutate(DictionaryEntry entry, TestInteraction interaction) {
        System.out.println("Applying unmatched regex mutation.");

        ExtendedRandom random = Environment.getInstance().getRandom();

        ParameterLeaf parameter = entry.getSource();

        if (isParameterMutable(parameter)) {
            ParameterLeaf mutatedParameter = new StringParameter(parameter);
            interaction.setMutateInfo("Unmatched Regex Mutation. Parameter '" + parameter.getName()
                    + "' does not match regex pattern '" + ((StringParameter) parameter).getPattern() + "'.");

            String pattern = ((StringParameter) parameter).getPattern();
            String[] components = pattern.split("\\[");
            String value = "";
            if (components.length > 2) {
                Map<String, Boolean> map = new LinkedHashMap<>();
                for (String component : components) {
                    if (!component.isEmpty() && !component.equals("^") && !component.equals("$")) {
                        if (component.startsWith("^")) {
                            component = component.substring(1);
                        }
                        if (component.contains("]")) {
                            component = "[" + component;
                        }
                        if (component.endsWith("$")) {
                            component = component.substring(0, component.length() - 1);
                        }

                        map.put(component, random.nextBoolean());
                    }
                }

                if (!map.containsValue(true)) {
                    map.put(map.keySet().iterator().next(), true);
                }

                for (Map.Entry<String, Boolean> pair : map.entrySet()) {
                    // True means generate non-matching string
                    System.out.println(pair.getKey());
                    System.out.println(pair.getValue());
                    RgxGen rgxGen = RgxGen.parse(pair.getKey());
                    value += pair.getValue() ? rgxGen.generateNotMatching() : rgxGen.generate();
                }
            }

            interaction.removeInput(entry);
            interaction.getRequestInputs().add(new DictionaryEntry(
                    mutatedParameter, value.equals("") ? RgxGen.parse(pattern).generateNotMatching() : value));

        } else {
            logger.warning("Could not apply mutation. This parameter is not of a mutable type.");
        }
    }
}
