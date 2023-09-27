package io.testrest.implementation;

import io.testrest.Environment;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.implementation.oracle.StatusCodeOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TestGenerator {

    private Environment environment;

    private String testOutPutPath;

    private static List<String> testFiles;

    private StatusCodeOracle statusCodeOracle;

    public TestGenerator() {
        environment = Environment.getInstance();
        testFiles = new ArrayList<>();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public abstract void generateTestBackground(String url, String filename);

    public abstract boolean generateOperationTest(OperationNode operation);

    public abstract void generateTest(OperationDependencyGraph ODG);

    public static List<String> getTestFiles() {
        return testFiles;
    }

    public static void setTestFiles(List<String> testFiles) {
        TestGenerator.testFiles = testFiles;
    }

    public static void addTestFile(String testFile) {
        testFiles.add(testFile);
    }

    public String getTestOutPutPath() {
        return testOutPutPath;
    }

    public void setTestOutPutPath(String outPutPath) {
        testOutPutPath = outPutPath;
    }

    public StatusCodeOracle getStatusCodeOracle() {
        return statusCodeOracle;
    }

    public void setStatusCodeOracle(StatusCodeOracle statusCodeOracle) {
        this.statusCodeOracle = statusCodeOracle;
    }

}
