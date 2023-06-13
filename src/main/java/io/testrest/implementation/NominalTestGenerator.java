package io.testrest.implementation;

import io.testrest.Environment;
import io.testrest.datatype.OperationNodeList;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.parser.OpenAPIParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NominalTestGenerator extends TestGenerator {

    public NominalTestGenerator(OperationNodeList operationNodeList, List<String> serverUrls) {
        super();
        setTestOutPutPath(Environment.getConfiguration().getOutputPath() + Environment.getConfiguration().getTestingSessionName() + "/NominalTests/");
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
        sb.append("\n\t\tGiven path '").append(operation.getPath()).append("'");
        // TODO: gen input
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
}
