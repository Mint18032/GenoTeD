package io.testrest;

import io.testrest.parser.OpenAPIParser;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final String openApiSpecPath = "specifications/openapi.yaml"; // path to openapi specification, can be either a link or a file

    public static void main(String[] args) {
        logger.info("Reading OpenAPI Specification.");
        try {
            OpenAPIParser.readOAS(openApiSpecPath);
        } catch (Exception e) {
            logger.warning(e.toString());
            e.printStackTrace();
        }

        logger.info("Read OpenAPI Specification Successfully. Starting Building Operation Dependency Graph.");
    }
}