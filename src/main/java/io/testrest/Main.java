package io.testrest;

import io.testrest.datatype.OperationNodeList;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.implementation.testGenerator.ErrorTestGenerator;
import io.testrest.implementation.GraphBuilder;
import io.testrest.implementation.testGenerator.NominalTestGenerator;
import io.testrest.parser.OpenAPIParser;
import io.testrest.testing.TestRunner;
import io.testrest.testing.TestSequence;

import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static Configuration configuration;

    private static Environment environment;

    private static String openApiSpecPath; // path to openapi specification, can be either a link or a file.

    private static OperationNodeList operationList;

    private static OperationDependencyGraph ODG;

    private static TestRunner testRunner;

    public static void main(String[] args) {

        environment = Environment.getInstance();
        configuration = Environment.getConfiguration();
        openApiSpecPath = Configuration.getOpenApiSpecPath();
        testRunner = new TestRunner();

        logger.info("Reading OpenAPI Specification.");
        try {
            operationList = new OperationNodeList();
            OpenAPIParser.readOAS(openApiSpecPath, operationList);
//            System.out.println(operationList);
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        logger.info("Successfully read the OpenAPI Specification. Starting building Operation Dependency Graph.");
        try {
            ODG = new OperationDependencyGraph();
            GraphBuilder.buildGraph(ODG);
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
        }

        logger.info("Successfully built the Operation Dependency Graph. Starting generating nominal testcases.");
        NominalTestGenerator nominalTestGenerator = new NominalTestGenerator(OpenAPIParser.getUrls());
        TestSequence nominalTestSequence = nominalTestGenerator.generateTest(ODG);
        logger.info("Nominal test cases are located at " + nominalTestGenerator.getTestOutPutPath());

        logger.info("Successfully generated the Nominal test cases. Starting generating error testcases.");
        ErrorTestGenerator errorTestGenerator = new ErrorTestGenerator(OpenAPIParser.getUrls());
        errorTestGenerator.generateTest(nominalTestSequence);
        logger.info("Successfully generated the Error test cases. \n");


//        logger.info("Running nominal test cases");
//        List<String> nominalTestPaths = nominalTestGenerator.getNominalTestPaths();
//        for (String path : nominalTestPaths) {
////            testRunner.testAll(path);
//        }

        logger.info("Running error test cases");
        List<String> errorTestPaths = errorTestGenerator.getErrorTestPaths();
        for (String path : errorTestPaths) {
            testRunner.testAll(path);
        }
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static OperationNodeList getOperationList() {
        return operationList;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(Environment environment) {
        Main.environment = environment;
    }

    public static TestRunner getTestRunner() {
        return testRunner;
    }

    public static void setTestRunner(TestRunner testRunner) {
        Main.testRunner = testRunner;
    }

}