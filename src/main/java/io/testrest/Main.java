package io.testrest;

import io.testrest.datatype.graph.OperationNodeList;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.core.testGenerator.ErrorTestGenerator;
import io.testrest.datatype.graph.GraphBuilder;
import io.testrest.core.testGenerator.NominalTestGenerator;
import io.testrest.parser.OpenAPIParser;
import io.testrest.core.testing.TestRunner;
import io.testrest.core.testing.TestSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static Configuration configuration;

    private static Environment environment;

    private static OperationNodeList operationList;

    private static OperationDependencyGraph ODG;

    public static void main(String[] args) {

        environment = Environment.getInstance();
        configuration = Environment.getConfiguration();
        String openApiSpecPath = Configuration.getOpenApiSpecPath();
        TestRunner testRunner = new TestRunner();

        logger.info("Reading OpenAPI Specification.");
        try {
            operationList = new OperationNodeList();
            OpenAPIParser.readOAS(openApiSpecPath, operationList);
            logger.info("Successfully read the OpenAPI Specification. Starting building Operation Dependency Graph.");
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            ODG = new OperationDependencyGraph();
            GraphBuilder.buildGraph(ODG);
            logger.info("Successfully built the Operation Dependency Graph. Starting generating nominal testcases.");
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
        }

        NominalTestGenerator nominalTestGenerator = new NominalTestGenerator(OpenAPIParser.getUrls());
        TestSequence nominalTestSequence = nominalTestGenerator.generateTest(ODG);
        List<String> allTestPaths = new ArrayList<>(nominalTestGenerator.getNominalTestPaths());
        logger.info("Nominal test cases are located at " + nominalTestGenerator.getTestOutPutPath());

        if (!nominalTestSequence.isEmpty()) {
            logger.info("Starting generating error testcases.");
            ErrorTestGenerator errorTestGenerator = new ErrorTestGenerator(OpenAPIParser.getUrls());
            errorTestGenerator.generateTest(nominalTestSequence);

            if (!errorTestGenerator.getTestSequence().isEmpty()) {
                logger.info("Successfully generated the Error test cases. \n");
                allTestPaths.addAll(errorTestGenerator.getErrorTestPaths());
            }
        }

        logger.info("Running test cases");
        testRunner.testAll(allTestPaths);
        testRunner.showReport();
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

}