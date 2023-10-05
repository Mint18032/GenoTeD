package io.testrest.implementation.testGenerator;

import io.testrest.Environment;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.implementation.mutator.ConstraintViolationMutator;
import io.testrest.implementation.mutator.MissingRequiredMutator;
import io.testrest.implementation.mutator.WrongTypeMutator;
import io.testrest.implementation.oracle.NominalTestOracle;
import io.testrest.testing.Mutator;
import io.testrest.testing.TestInteraction;
import io.testrest.testing.TestSequence;
import org.jgrapht.alg.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ErrorTestGenerator extends TestGenerator {
    private List<String> errorTestPaths = new ArrayList<>();
    private final Set<Mutator> mutators;

    public ErrorTestGenerator(List<String> serverUrls) {
        super();

        setStatusCodeOracle(new NominalTestOracle());
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + "/ErrorTests/");
        for (String url : serverUrls) {
            String filename = (serverUrls.size() > 1 ? "TestServer" + serverUrls.indexOf(url) : "Tests") + ".feature";
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

            // Get last interaction in the sequence
            TestInteraction mutableInteraction = interaction.deepClone();
            mutableInteraction.setRequestInputs(interaction.getRequestInputs());
            mutableInteraction.addTag("mutated");

            // Get set of applicable mutations to this operation
            Set<Pair<DictionaryEntry, Mutator>> mutablePairs = new HashSet<>();
            mutableInteraction.getRequestInputs().forEach(entry -> mutators.forEach(mutator -> {
                if (mutator.isParameterMutable(entry.getSource())) {
                    mutablePairs.add(new Pair<>(entry, mutator));
                }
            }));

            // For each sequence, we generate n mutants for the last interaction
            for (int j = 0; j < numberOfMutants; j++) {

                // Choose a random mutation pair
                Optional<Pair<DictionaryEntry, Mutator>> mutable = Environment.getInstance().getRandom().nextElement(mutablePairs);

                if (mutable.isPresent()) {

                    try {
                        System.out.println("BEFORE MUTATION: \n" + mutableInteraction.getRequestInputs().toString());

                        // Apply mutation
                        mutable.get().getSecond().mutate(mutable.get().getFirst(), mutableInteraction);

                        // TODO: Xét điều kiện thành công
                            logger.fine("Mutation applied correctly.");
                            System.out.println("AFTER MUTATION: \n" + mutableInteraction.getRequestInputs().toString());
                            mutablePairs.remove(mutable.get());

                    } catch (Exception e) {
                        logger.warning("Could not apply mutation.");
                    }

                    // Run & evaluate sequence with oracles

                    boolean success = generateOperationTest(mutableInteraction.getOperation());

                    if (success) {
                        testSequence.add(mutableInteraction);
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
        sb.append("Feature: Basic Error tests\n");
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

    public boolean generateOperationTest(OperationNode operation) {
        return true;
    }

    public List<String> getErrorTestPaths() {
        return errorTestPaths;
    }

    public void setErrorTestPaths(List<String> errorTestPaths) {
        this.errorTestPaths = errorTestPaths;
    }

}
