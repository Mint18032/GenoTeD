package io.testrest.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.testrest.datatype.Method;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.OperationNodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAPIParser {
    private static OpenAPI openAPI;
    private static List<Server> servers = new ArrayList<>();
    private static List<String> urls = new ArrayList<>();
    private static Paths paths;
    private static List<String> pathUrls = new ArrayList<>();

    /**
     * Reads OpenAPI from the given path/link and parses it to Java POJOs.
     * @param openApiSpecPath the path or url to the specification.
     * @throws Exception Throws exception when the actions get error or the openAPI missing necessary parameter.
     */
    public static void readOAS(String openApiSpecPath, OperationNodeList operationList) throws Exception {
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
        readOperations(operationList);
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
    public static void readOperations(OperationNodeList operationList) throws CannotParseOperationException {
        paths = openAPI.getPaths();
        for (Map.Entry<String, PathItem> m : paths.entrySet()) {
            // m.key is the path
            // m.value is the pathItem which contains tags, summary, description and Operations.
            PathItem pathItem;
            try {
                pathItem = m.getValue();
                pathUrls.add(m.getKey());
                if (pathItem.getGet() != null) {
                    operationList.addOperation(new OperationNode(Method.GET, m.getKey(), pathItem.getGet()));
                }
                if (pathItem.getPut() != null) {
                    operationList.addOperation(new OperationNode(Method.PUT, m.getKey(), pathItem.getPut()));
                }
                if (pathItem.getPost() != null) {
                    operationList.addOperation(new OperationNode(Method.POST, m.getKey(), pathItem.getPost()));
                }
                if (pathItem.getDelete() != null) {
                    operationList.addOperation(new OperationNode(Method.DELETE, m.getKey(), pathItem.getDelete()));
                }
                if (pathItem.getOptions() != null) {
                    operationList.addOperation(new OperationNode(Method.OPTIONS, m.getKey(), pathItem.getOptions()));
                }
                if (pathItem.getHead() != null) {
                    operationList.addOperation(new OperationNode(Method.HEAD, m.getKey(), pathItem.getHead()));
                }
                if (pathItem.getPatch() != null) {
                    operationList.addOperation(new OperationNode(Method.PATCH, m.getKey(), pathItem.getPatch()));
                }
                if (pathItem.getTrace() != null) {
                    operationList.addOperation(new OperationNode(Method.TRACE, m.getKey(), pathItem.getTrace()));
                }
            } catch (Exception e) {
                throw new CannotParseOperationException("Unable to cast paths.entrySet().getValue() to PathItems:\n" + e);
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

    public static List<String> getPathUrls() {
        return pathUrls;
    }

    public static void setPathUrls(List<String> pathUrls) {
        OpenAPIParser.pathUrls = pathUrls;
    }

}

