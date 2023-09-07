package io.testrest.implementation;

import io.testrest.Environment;
import io.testrest.datatype.graph.OperationNode;

import java.util.ArrayList;
import java.util.List;

public abstract class TestGenerator {

    private Environment environment;

    private String testOutPutPath;

    private static List<String> testFiles;

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

    public abstract void generateOperationTest(OperationNode operation, String filename);

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
}
