package io.testrest.implementation;

import io.testrest.Environment;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.OperationNodeList;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.parameter.NormalizedParameterName;
import io.testrest.datatype.parameter.ParameterLeaf;
import io.testrest.datatype.parameter.ParameterLocation;
import io.testrest.implementation.parameterValueProvider.single.RandomParameterValueProvider;
import io.testrest.parser.OpenAPIParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NominalTestGenerator extends TestGenerator {
    private static Map<NormalizedParameterName, String> testinputs = new HashMap<>();
    private RandomParameterValueProvider valueProvider = new RandomParameterValueProvider();
    private List<String> nominalTestPaths = new ArrayList<>();

    /**
     * Initializes generator and generate testcases for all params of each operation of every path, the consequence is based on CRUD semantic.
     * Priorities: HEAD -> POST -> GET -> PUT & PATCH -> OPTIONS -> TRACE -> DELETE
     * @param operationNodeList
     * @param serverUrls
     */
    public NominalTestGenerator(OperationNodeList operationNodeList, List<String> serverUrls) {
        super();
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + "/NominalTests/");
        for (String url : serverUrls) {
            String filename = (serverUrls.size() > 1 ? "TestServer" + serverUrls.indexOf(url) : "Tests") + ".feature";
            nominalTestPaths.add(getTestOutPutPath().substring(getTestOutPutPath().indexOf("output/")).concat(filename));
            generateTestBackground(url, filename);
        }
        for(String path : OpenAPIParser.getPathUrls()) {
            List<OperationNode> headOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.HEAD))).collect(Collectors.toList());
            for(OperationNode operation : headOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> postOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.POST))).collect(Collectors.toList());
            for(OperationNode operation : postOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> getOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.GET))).collect(Collectors.toList());
            for(OperationNode operation : getOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> putOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.PUT))).collect(Collectors.toList());
            for(OperationNode operation : putOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> patchOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.PATCH))).collect(Collectors.toList());
            for(OperationNode operation : patchOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> optionsOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.OPTIONS))).collect(Collectors.toList());
            for(OperationNode operation : optionsOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> traceOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.TRACE))).collect(Collectors.toList());
            for(OperationNode operation : traceOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
            List<OperationNode> deleteOperations = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path) && operationNode.getMethod().equals(HttpMethod.DELETE))).collect(Collectors.toList());
            for(OperationNode operation : deleteOperations) {
                // gen test
                for (String url : getTestFiles()) {
                    generateOperationTest(operation, url);
                }
            }
        }

    }
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
//            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred. Could not create background condition");
            e.printStackTrace();
        }
    }

    public void generateOperationTest(OperationNode operation, String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\t@").append(operation.getOperationId());
        sb.append("\n\tScenario: ").append(operation.getOperationId());
//        if (!operation.getParameterLeafList().isEmpty()) {
//            sb.append("\n\t\t* def param =");
//            sb.append("\n\t\t\"\"\"").append("\n\t\t{");
//            sb.append("\n\t\t}").append("\n\t\t\"\"\"");
//        }
        String path = operation.getPath().contains("{") ? operation.getPath().substring(0,operation.getPath().indexOf("{")) : operation.getPath();
        sb.append("\n\t\tGiven path '").append(path).append("'");
        for(ParameterLeaf parameterLeaf : operation.getParameterLeafList()) {
            if (parameterLeaf.getLocation() == ParameterLocation.PATH || parameterLeaf.getLocation() == ParameterLocation.MISSING)
                sb.append(generatePathInput(parameterLeaf));
        }
        for(ParameterLeaf parameterLeaf : operation.getParameterLeafList()) {
            if (parameterLeaf.getLocation() == ParameterLocation.QUERY)
                sb.append(generateQueryInput(parameterLeaf));
        }
//        if (!operation.getParameterLeafList().isEmpty()) {
//            sb.append("\n\t\tAnd request param");
//        }
        sb.append("\n\t\tWhen method ").append(operation.getMethod());

//        TODO: other status
        String response_status = operation.getResponses().entrySet().stream()
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("200");
        sb.append("\n\t\tThen status ").append(response_status.contains("default") ? "200" : response_status);
        sb.append("\n\t\tAnd print response");

        try {
            FileWriter myWriter = new FileWriter(filename, true);
            myWriter.write(sb.toString());
            myWriter.close();
//            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred. Could not create background condition");
            e.printStackTrace();
        }
    }

    public String generatePathInput(ParameterLeaf parameterLeaf) {
        for (Map.Entry<NormalizedParameterName, String> testinput : testinputs.entrySet()) {
            if(testinput.getKey().equals(parameterLeaf.getNormalizedName())) {
                return testinput.getValue();
            }
        }
        StringBuilder input = new StringBuilder();
        input.append(", '").append(valueProvider.provideValueFor(parameterLeaf)).append("'");

        testinputs.put(parameterLeaf.getNormalizedName(), input.toString());

        return input.toString();
    }

    public String generateQueryInput(ParameterLeaf parameterLeaf) {
        for (Map.Entry<NormalizedParameterName, String> testinput : testinputs.entrySet()) {
            if(testinput.getKey().equals(parameterLeaf.getNormalizedName())) {
                return testinput.getValue();
            }
        }
        StringBuilder input = new StringBuilder();
        input.append("\n\t\tAnd param ").append(parameterLeaf.getName().toString()).append(" = ");
        input.append("\"").append(valueProvider.provideValueFor(parameterLeaf)).append("\"");

        testinputs.put(parameterLeaf.getNormalizedName(), input.toString());

        return input.toString();
    }

    public static Map<NormalizedParameterName, String> getTestinputs() {
        return testinputs;
    }

    public static void setTestinputs(Map<NormalizedParameterName, String> testinputs) {
        NominalTestGenerator.testinputs = testinputs;
    }

    public RandomParameterValueProvider getValueProvider() {
        return valueProvider;
    }

    public void setValueProvider(RandomParameterValueProvider valueProvider) {
        this.valueProvider = valueProvider;
    }


    public List<String> getNominalTestPaths() {
        return nominalTestPaths;
    }

    public void setNominalTestPaths(List<String> nominalTestPaths) {
        this.nominalTestPaths = nominalTestPaths;
    }
}
