package io.testrest.core.testGenerator;

import io.testrest.Environment;
import io.testrest.boot.AuthenticationInfo;
import io.testrest.core.oracle.StatusCodeOracle;
import io.testrest.core.testing.TestSequence;

import java.util.ArrayList;
import java.util.List;

public abstract class TestGenerator {

    private String testOutPutPath;

    private static List<String> testFiles;

    protected TestSequence testSequence;

    private StatusCodeOracle statusCodeOracle;

    protected AuthenticationInfo authenticationInfo = Environment.getConfiguration().getAuthenticationInfo();

    public TestGenerator() {
        testFiles = new ArrayList<>();
        testSequence = new TestSequence();
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
