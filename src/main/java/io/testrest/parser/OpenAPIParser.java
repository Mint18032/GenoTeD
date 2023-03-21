package io.testrest.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.testrest.datatype.OperationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAPIParser {
    private static OpenAPI openAPI;
    private static List<OperationNode> operations = new ArrayList<>();
    private static List<Server> servers = new ArrayList<>();
    private static List<String> urls = new ArrayList<>();
    private static Paths paths;

    /**
     * Reads OpenAPI from the given path/link and parses it to Java POJOs.
     * @param openApiSpecPath the path or url to the specification.
     * @throws Exception Throws exception when the actions get error or the openAPI missing necessary parameter.
     */
    public static void readOAS(String openApiSpecPath) throws Exception {
        // Read OpenAPI Specification and parse it to POJO type
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        SwaggerParseResult result = new io.swagger.parser.OpenAPIParser().readLocation(openApiSpecPath, null, parseOptions);
        openAPI = result.getOpenAPI();

        if (result.getMessages() == null || openAPI == null) {
            throw new CannotParseOpenAPIException("getOpenAPI() returns null.");
        }

        // Validate
        OpenAPIValidator.validate(openAPI);

        // Read data
        readURLs();
        readOperations();
        System.out.println(operations);
    }

    /**
     * Reads the URLs of the API from the specification.
     */
    public static void readURLs() throws NoServerUrlFoundException {
        servers = openAPI.getServers();
        for (Server s: servers) {
            urls.add(s.getUrl());
        }
        if (urls == null) {
            throw new NoServerUrlFoundException("No URL found among the API servers.");
        }
    }

    /**
     * Reads the operations of the API from the specification, transfers them to OperationVectors and adds to List.
     */
    public static void readOperations() throws CannotParseOperationException {
        paths = openAPI.getPaths();
        for (Map.Entry m : paths.entrySet()) {
            // m.key is the path
            // m.value is the pathItem which contains tags, summary, description and Operations.
            PathItem pathItem;
            if (m.getValue() instanceof PathItem) {
                pathItem = (PathItem) m.getValue();
                if (pathItem.getGet() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.GET, m.getKey().toString(), pathItem.getGet()));
                }
                if (pathItem.getPut() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.PUT, m.getKey().toString(), pathItem.getPut()));
                }
                if (pathItem.getPost() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.POST, m.getKey().toString(), pathItem.getPost()));
                }
                if (pathItem.getDelete() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.DELETE, m.getKey().toString(), pathItem.getDelete()));
                }
                if (pathItem.getOptions() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.OPTIONS, m.getKey().toString(), pathItem.getOptions()));
                }
                if (pathItem.getHead() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.HEAD, m.getKey().toString(), pathItem.getHead()));
                }
                if (pathItem.getPatch() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.PATCH, m.getKey().toString(), pathItem.getPatch()));
                }
                if (pathItem.getTrace() != null) {
                    operations.add(new OperationNode(OperationNode.METHOD.TRACE, m.getKey().toString(), pathItem.getTrace()));
                }
            } else {
                throw new CannotParseOperationException("Unable to cast paths.entrySet().getValue() to PathItems");
            }
        }
    }

    public static void setOpenAPI(OpenAPI openAPI) {
        OpenAPIParser.openAPI = openAPI;
    }

    public static OpenAPI getOpenAPI() {
        return openAPI;
    }

    public static void setUrls(List<String> urls) {
        OpenAPIParser.urls = urls;
    }

    public static List<String> getUrls() {
        return urls;
    }

    public static void setOperations(List<OperationNode> operations) {
        OpenAPIParser.operations = operations;
    }

    public static List<OperationNode> getOperations() {
        return operations;
    }

    public static Paths getPaths() {
        return paths;
    }

    public static void setPaths(Paths paths) {
        OpenAPIParser.paths = paths;
    }

    public static List<Server> getServers() {
        return servers;
    }

    public static void setServers(List<Server> servers) {
        OpenAPIParser.servers = servers;
    }
}

