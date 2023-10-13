package io.testrest.implementation.testGenerator;

import io.testrest.Environment;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.ParameterLocation;
import io.testrest.dictionary.DictionaryEntry;
import io.testrest.implementation.oracle.NominalTestOracle;
import io.testrest.testing.parameterValueProvider.multi.CombinedProviderParameterValueProvider;
import io.testrest.testing.OperationsSorter;
import io.testrest.testing.TestInteraction;
import io.testrest.testing.TestSequence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class NominalTestGenerator extends TestGenerator {
    protected Logger logger = Logger.getLogger(NominalTestGenerator.class.getName());
    private final CombinedProviderParameterValueProvider parameterValueProvider = new CombinedProviderParameterValueProvider();
    private List<String> nominalTestPaths = new ArrayList<>();
    private List<DictionaryEntry> pendingEntries = new LinkedList<>();

    /**
     * Initializes generator and generate testcases for all params of each operation of every path, the consequence is based on CRUD semantic.
     * Priorities: HEAD -> POST -> GET -> PUT & PATCH -> OPTIONS -> TRACE -> DELETE
     * @param serverUrls
     */
    public NominalTestGenerator(List<String> serverUrls) {
        super();
        setStatusCodeOracle(new NominalTestOracle());
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + "/NominalTests/");
        for (String url : serverUrls) {
            String filename = (serverUrls.size() > 1 ? "TestServer" + serverUrls.indexOf(url) : "Tests") + ".feature";
            addTestFile(filename);
            nominalTestPaths.add(getTestOutPutPath().substring(getTestOutPutPath().indexOf("output/")).concat(filename));
            generateTestBackground(url, filename);
        }
    }

    /**
     * Main test generate and validate function.
     * @param ODG Operation Dependencies Graph.
     */
    public TestSequence generateTest(OperationDependencyGraph ODG) {
        int maxFuzzingTimes = Environment.getConfiguration().getMaxFuzzingTimes();

        while (ODG.getGraph().vertexSet().size() > 0) {
            List<OperationNode> nodeToTest = ODG.getLeaves();

            if (nodeToTest.size() == 0) {
                nodeToTest = ODG.getNextDependentNodes();
            }

            nodeToTest = OperationsSorter.semanticSort(nodeToTest);

            // Test each operation
            for (OperationNode operationNode : nodeToTest) {
                while (operationNode.getTestedTimes() <= maxFuzzingTimes) {
                    boolean success = generateOperationTest(operationNode);
                    operationNode.markAsTested();

                    // Remove successfully tested nodes
                    if (success || operationNode.getTestedTimes() == maxFuzzingTimes) {
                        ODG.getGraph().removeVertex(operationNode);
                        break;
                    }
                }
            }
        }

        return testSequence;
    }

    @Override
    public void generateTestBackground(String url, String filename) {
        File file = new File(getTestOutPutPath());
        file.mkdirs();

        String filePath = getTestOutPutPath() + filename;
        addTestFile(filePath);

        StringBuilder sb = new StringBuilder();
        sb.append("Feature: Basic Nominal tests\n");
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
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\t@").append(operation.getOperationId());
        sb.append("\n\tScenario: ").append(operation.getOperationId());

        if (operation.containsHeader()) {
            sb.append("\n\t\t* configure headers = {");
            for(ParameterLeaf parameterLeaf : operation.getParameterLeafList()) {
                if (parameterLeaf.getLocation() == ParameterLocation.HEADER)
                    sb.append(generateHeaderInput(parameterLeaf));
            }
            sb.append(" }");
        }

        String path = operation.getPath();
        for(ParameterLeaf parameterLeaf : operation.getParameterLeafList()) {
            if (parameterLeaf.getLocation() == ParameterLocation.PATH || parameterLeaf.getLocation() == ParameterLocation.MISSING)
                path = generatePathInput(parameterLeaf, path);
        }
        sb.append("\n\t\tGiven path \"").append(path).append("\"");

        for(ParameterLeaf parameterLeaf : operation.getParameterLeafList()) {
            if (parameterLeaf.getLocation() == ParameterLocation.QUERY)
                sb.append(generateQueryInput(parameterLeaf));
        }

        sb.append("\n\t\tWhen method ").append(operation.getMethod());

        String response_status = operation.getResponses().entrySet().stream()
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("200");
        sb.append("\n\t\tThen status ").append(response_status.contains("default") ? "200" : response_status);
        sb.append("\n\t\tAnd print response");

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

        boolean statusCodePassed = getStatusCodeOracle().assessOperationTest(operation, getNominalTestPaths());

        if (statusCodePassed) {
            // add pending entries of successfully generated testcases to Dictionary
            pendingEntries.forEach(dictionaryEntry -> {
                getEnvironment().getGlobalDictionary().addEntry(dictionaryEntry);
            });

            // generate new test interaction
            testSequence.append(new TestInteraction(operation, pendingEntries));
        }

        pendingEntries.clear();

        return statusCodePassed;
    }

    public String generatePathInput(ParameterLeaf parameterLeaf, String path) {
        Object value = this.parameterValueProvider.provideValueFor(parameterLeaf);
        path = path.replace("{" + parameterLeaf.getName() + "}", value.toString());

        pendingEntries.add(new DictionaryEntry(parameterLeaf, value));

        return path;
    }

    public String generateQueryInput(ParameterLeaf parameterLeaf) {
        StringBuilder input = new StringBuilder();
        Object value = this.parameterValueProvider.provideValueFor(parameterLeaf);
        input.append("\n\t\tAnd param ").append(parameterLeaf.getName().toString()).append(" = ");
        input.append("\"").append(value).append("\"");

        pendingEntries.add(new DictionaryEntry(parameterLeaf, value));

        return input.toString();
    }

    public String generateHeaderInput(ParameterLeaf parameterLeaf) {
        StringBuilder input = new StringBuilder();
        Object value = this.parameterValueProvider.provideValueFor(parameterLeaf);
        input.append(" '").append(parameterLeaf.getName().toString()).append("' : ");
        input.append("'").append(value).append("',");

        pendingEntries.add(new DictionaryEntry(parameterLeaf, value));

        return input.toString();
    }

    public String generateBodyInput(ParameterLeaf parameterLeaf) {
        StringBuilder input = new StringBuilder();
        Object value = this.parameterValueProvider.provideValueFor(parameterLeaf);
        input.append("\n\t\tAnd param ").append(parameterLeaf.getName().toString()).append(" = ");
        input.append("\"").append(value).append("\"");

        pendingEntries.add(new DictionaryEntry(parameterLeaf, value));

        return input.toString();
    }

    public List<String> getNominalTestPaths() {
        return nominalTestPaths;
    }

    public void setNominalTestPaths(List<String> nominalTestPaths) {
        this.nominalTestPaths = nominalTestPaths;
    }

    public List<DictionaryEntry> getPendingEntries() {
        return pendingEntries;
    }

    public void setPendingEntries(List<DictionaryEntry> pendingEntries) {
        this.pendingEntries = pendingEntries;
    }
}
