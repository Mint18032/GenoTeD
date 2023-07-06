package io.testrest.implementation;

import io.testrest.Environment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NominalTestGenerator extends TestGenerator {
    private static Map<NormalizedParameterName, String> testinputs = new HashMap<>();
    private RandomParameterValueProvider valueProvider = new RandomParameterValueProvider();

    public NominalTestGenerator(OperationNodeList operationNodeList, List<String> serverUrls) {
        super();
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + "/NominalTests/");
        for (String url : serverUrls) {
            generateTestBackground(url, (serverUrls.size() > 1 ? "TestServer" + serverUrls.indexOf(url) : "Tests") + ".feature");
        }
        for(String path : OpenAPIParser.getPathUrls()) {
            List<OperationNode> samePathNodes = operationNodeList.getOperationNodeList().stream().filter(operationNode ->
                    (operationNode.getPath().equals(path))).collect(Collectors.toList());
            for(OperationNode operation : samePathNodes) {
                // gen test
                for (String url :
                        getTestFiles()) {
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
        // TODO: gen input
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
        sb.append("\n\t\tThen status ").append(operation.getResponses().entrySet().stream()
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null));
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
}
