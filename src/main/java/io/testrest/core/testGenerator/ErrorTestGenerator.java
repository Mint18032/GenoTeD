package io.testrest.core.testGenerator;

import io.testrest.Environment;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.parameter.ParameterLocation;
import io.testrest.datatype.parameter.ParameterName;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.core.mutator.ConstraintViolationMutator;
import io.testrest.core.mutator.MissingRequiredMutator;
import io.testrest.core.mutator.WrongTypeMutator;
import io.testrest.core.oracle.ErrorTestOracle;
import io.testrest.core.mutator.Mutator;
import io.testrest.core.testing.TestInteraction;
import io.testrest.core.testing.TestSequence;
import org.jgrapht.alg.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ErrorTestGenerator extends TestGenerator {
    protected Logger logger = Logger.getLogger(ErrorTestGenerator.class.getName());
    private List<String> errorTestPaths = new ArrayList<>();
    private final Set<Mutator> mutators;

    public ErrorTestGenerator(List<String> serverUrls) {
        super();

        setStatusCodeOracle(new ErrorTestOracle());
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + "/ErrorTests/");
        for (String url : serverUrls) {
            String filename = (serverUrls.size() > 1 ? "TestServer" + (serverUrls.indexOf(url)+1) : "Tests") + ".feature";
            addTestFile(filename);
            errorTestPaths.add(getTestOutPutPath().substring(getTestOutPutPath().indexOf("output/")).concat(filename));
            generateTestBackground(url, filename);
        }

        mutators = new HashSet<>();
        mutators.add(new MissingRequiredMutator());
        mutators.add(new WrongTypeMutator());
        mutators.add(new ConstraintViolationMutator());
    }

    /**
     * Main test generate and validate function.
     */
    public void generateTest(TestSequence nominalTestSequence) {
        int numberOfMutants = Environment.getConfiguration().getNumberOfMutants();

        // Iterate on interaction of test sequence
        for (TestInteraction interaction : nominalTestSequence) {
            // Get set of applicable mutations to this operation
            Set<Pair<DictionaryEntry, Mutator>> mutablePairs = new HashSet<>();
            interaction.getRequestInputs().forEach(entry -> mutators.forEach(mutator -> {
                if (mutator.isParameterMutable(entry.getSource())) {
                    mutablePairs.add(new Pair<>(entry, mutator));
                }
            }));

            // For each sequence, we generate n mutants for the last interaction
            for (int j = 0; j < numberOfMutants && mutablePairs.size() > 0; j++) {

                // Get last interaction in the sequence
                TestInteraction mutableInteraction = interaction.deepClone();
                mutableInteraction.addTag("mutated");

                // Choose a random mutation pair
                Optional<Pair<DictionaryEntry, Mutator>> mutable = Environment.getInstance().getRandom().nextElement(mutablePairs);

                if (mutable.isPresent()) {

                    try {

                        // Apply mutation
                        mutable.get().getSecond().mutate(mutable.get().getFirst(), mutableInteraction);
                        mutablePairs.remove(mutable.get());

                        // Add new pairs to list in case all pairs are used.
                        if (mutablePairs.size() == 0) {
                            mutators.clear();
                            mutators.add(new ConstraintViolationMutator());
                            mutators.add(new WrongTypeMutator());

                            interaction.getRequestInputs().forEach(entry -> mutators.forEach(mutator -> {
                                if (mutator.isParameterMutable(entry.getSource())) {
                                    mutablePairs.add(new Pair<>(entry, mutator));
                                }
                            }));
                        }

                        // Run & evaluate sequence with oracles
                        boolean success = generateOperationTest(mutableInteraction);

                        if (success) {
                            System.out.println("BEFORE MUTATION: \n" + interaction.getRequestInputs().toString());
                            System.out.println("AFTER MUTATION: \n" + mutableInteraction.getRequestInputs().toString());

                            testSequence.add(mutableInteraction);
                        }

                    } catch (Exception e) {
                        logger.warning("Could not apply mutation.");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void generateTestBackground(String url, String filename) {
        File file = new File(getTestOutPutPath());
        file.mkdirs();

        String filePath = getTestOutPutPath() + filename;
        addTestFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("Feature: ").append(filename.contains("Server") ? filename.substring(4, filename.indexOf(".")) + " error tests\n" : "Error tests\n");
        sb.append("\n\tBackground:\n\t\tGiven url '" + url + "'");

        try {
            FileWriter myWriter = new FileWriter(filePath, true);
            myWriter.write(sb.toString());
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred. Could not create background condition");
            e.printStackTrace();
        }
    }

    public boolean generateOperationTest(TestInteraction interaction) {
        OperationNode operation = interaction.getOperation();
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n\t@").append(operation.getOperationId());
        sb.append("\n\tScenario: ").append(operation.getOperationId()).append(" ").append(interaction.getMutateInfo());

        if (operation.containsHeader()) {
            String temp_string = "\n\t\t* configure headers = {";
            boolean added_header = false;
            for(DictionaryEntry entry : interaction.getRequestInputs()) {
                if (entry.getSource().getLocation() == ParameterLocation.HEADER) {
                    temp_string = temp_string.concat(generateHeaderInput(entry.getSource().getName(), entry.getValue().toString()));
                    added_header = true;
                }
            }
            temp_string = temp_string.concat(" }");

            if (added_header) {
                sb.append(temp_string);
            }
        }

        String path = operation.getPath();
        for(DictionaryEntry entry : interaction.getRequestInputs()) {
            if (entry.getSource().getLocation() == ParameterLocation.PATH || entry.getSource().getLocation() == ParameterLocation.MISSING)
                path = generatePathInput(entry.getSource().getName(), entry.getValue().toString(), path);
        }
        sb.append("\n\t\tGiven path \"").append(path).append("\"");

        for(DictionaryEntry entry : interaction.getRequestInputs()) {
            if (entry.getSource().getLocation() == ParameterLocation.QUERY)
                sb.append(generateQueryInput(entry.getSource().getName(), entry.getValue().toString()));
        }

        sb.append("\n\t\tWhen method ").append(operation.getMethod());
        sb.append("\n\t\tAnd print response");
        sb.append("\n\t\tAnd print responseStatus");
        sb.append("\n\t\tThen assert responseStatus >= 400 && responseStatus < 500");

        for (String filename : getTestFiles()) {
            try {
                FileWriter myWriter = new FileWriter(filename, true);
                myWriter.write(sb.toString());
                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred. Could not write testcases to file " + filename);
                e.printStackTrace();
            }
        }

        return getStatusCodeOracle().assessOperationTest(operation, getErrorTestPaths());
    }

    public String generatePathInput(ParameterName parameterName, String value, String path) {
        return path.replace("{" + parameterName.toString() + "}", value);
    }

    public String generateQueryInput(ParameterName parameterName, String value) {
        return "\n\t\tAnd param " + parameterName.toString() + " = " +
                "\"" + value + "\"";
    }

    public String generateHeaderInput(ParameterName parameterName, String value) {
        return " '" + parameterName.toString() + "' : " +
                "'" + value + "',";
    }

    public String generateBodyInput(ParameterName parameterName, String value) {
        return "\n\t\tAnd param " + parameterName.toString() + " = " +
                "\"" + value + "\"";
    }

    public List<String> getErrorTestPaths() {
        return errorTestPaths;
    }

    public void setErrorTestPaths(List<String> errorTestPaths) {
        this.errorTestPaths = errorTestPaths;
    }

}
