package io.testrest.implementation.testGenerator;

import io.testrest.Environment;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.implementation.oracle.StatusCodeOracle;
import io.testrest.testing.TestSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class TestGenerator {
    protected Logger logger = Logger.getLogger(TestGenerator.class.getName());

    private Environment environment;

    private String testOutPutPath;

    private static List<String> testFiles;

    protected TestSequence testSequence;

    private StatusCodeOracle statusCodeOracle;

    public TestGenerator() {
        environment = Environment.getInstance();
        testFiles = new ArrayList<>();
        testSequence = new TestSequence();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public abstract void generateTestBackground(String url, String filename);

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

    public TestSequence getTestSequence() {
        return testSequence;
    }

    public void setTestSequence(TestSequence testSequence) {
        this.testSequence = testSequence;
    }

    public StatusCodeOracle getStatusCodeOracle() {
        return statusCodeOracle;
    }

    public void setStatusCodeOracle(StatusCodeOracle statusCodeOracle) {
        this.statusCodeOracle = statusCodeOracle;
    }

}
