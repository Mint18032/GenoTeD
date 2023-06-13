package io.testrest;

import io.testrest.datatype.OperationNodeList;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.implementation.GraphBuilder;
import io.testrest.implementation.NominalTestGenerator;
import io.testrest.parser.OpenAPIParser;
import io.testrest.testing.TestRunner;

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
        }

        logger.info("Successfully read the OpenAPI Specification. \nStarting building Operation Dependency Graph.");
        try {
            ODG = new OperationDependencyGraph();
            GraphBuilder.buildGraph(ODG);
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
        }

        logger.info("Successfully built the Operation Dependency Graph. \nStarting generating nominal testcases.");
        NominalTestGenerator nominalTestGenerator = new NominalTestGenerator(operationList, OpenAPIParser.getUrls());
        logger.info("Nominal test cases are located at " + nominalTestGenerator.getTestOutPutPath());
        logger.info("Running nominal test cases");
        String path = nominalTestGenerator.getTestOutPutPath().substring(nominalTestGenerator.getTestOutPutPath().indexOf("output/")).concat("Tests.feature");
//        testRunner.testAll(path);
        testRunner.testOperation(path, "v2All");
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