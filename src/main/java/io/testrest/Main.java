package io.testrest;

import io.testrest.datatype.OperationNodeList;
import io.testrest.datatype.graph.OperationDependencyGraph;
import io.testrest.implementation.CannotBuildODGException;
import io.testrest.implementation.GraphBuilder;
import io.testrest.parser.OpenAPIParser;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static Configuration configuration = new Configuration();

    private static final String openApiSpecPath = "specifications/openapi.yaml"; // path to openapi specification, can be either a link or a file.

    private static OperationNodeList operationList;

    private static OperationDependencyGraph ODG;

    public static void main(String[] args) {

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

//        logger.info("Successfully built the Operation Dependency Graph. \nStarting generating nominal testcases.");
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static OperationNodeList getOperationList() {
        return operationList;
    }
}