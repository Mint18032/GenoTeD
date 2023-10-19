package io.testrest.parser;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.testrest.Environment;
import io.testrest.datatype.HttpMethod;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.datatype.graph.OperationNodeList;
import io.testrest.datatype.normalizer.PathNormalizer;
import io.testrest.helper.ExtendedRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class OpenAPIParser {
    private static final Logger logger = Logger.getLogger(OpenAPIParser.class.getName());
    private static final ExtendedRandom random = Environment.getInstance().getRandom();
    private static OpenAPI openAPI;
    private static List<Server> servers = new ArrayList<>();
    private static List<String> urls = new ArrayList<>(); // servers' Urls
    private static Paths paths;
    private static Components components = new Components();
    private static List<String> pathUrls = new ArrayList<>();

    private static Map<String, Schema> schema;

    /**
     * Reads OpenAPI from the given path/link and parses it to Java POJOs.
     * @param openApiSpecPath the path or url to the specification.
     * @throws Exception Throws exception when the actions get error or the openAPI missing necessary parameter.
     */
    public static void readOAS(String openApiSpecPath, OperationNodeList operationList) throws Exception {
        // Read OpenAPI Specification and parse it to POJO type
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        openAPI = new OpenAPIV3Parser().read(openApiSpecPath, null, parseOptions);

        if (openAPI == null) {
            throw new CannotParseOpenAPIException("getOpenAPI() returns null.");
        }

        // Validate
        OpenAPIValidator.validate(openAPI);

        // Read data
        readURLs();
        components = openAPI.getComponents();
        schema = components == null ? new HashMap<>() : components.getSchemas();
        readOperations(operationList);

    }

    /**
     * Reads the URLs of the API from the specification.
     */
    public static void readURLs() throws NoServerUrlFoundException {
        servers = openAPI.getServers();
        for (Server s: servers) {
            if (OpenAPIValidator.isValidServer(s.getUrl())) {
                String url = s.getUrl();
                // if server url has variables, replace them with relevant values
                if (s.getVariables() != null) {
                    for (Map.Entry<String, ServerVariable> serverVariable : s.getVariables().entrySet()) {
                        String var_name = serverVariable.getKey();
                        String var_value = serverVariable.getValue().getEnum() == null ? serverVariable.getValue().getDefault() : serverVariable.getValue().getEnum().get(random.nextInt(0, serverVariable.getValue().getEnum().size()));
                        url = url.replace("{" + var_name + "}", var_value);
                    }
                }

                // servers having the same url are considered 1
                if (!urls.contains(url))
                    urls.add(url);
                else
                    logger.warning("Server " + url + " is duplicated and will be tested one time only.");
            }
        }
        if (urls == null) {
            throw new NoServerUrlFoundException("No valid URL found among the API servers.");
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

            try {
                String normalized_path_url = PathNormalizer.normalize(m.getKey());
                pathUrls.add(normalized_path_url);
                PathItem pathItem = m.getValue();
//                System.out.println(m.getValue());
                if (pathItem.getHead() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.HEAD, normalized_path_url, pathItem.getHead()));
                }
                if (pathItem.getPost() != null) {
                    try {

                        operationList.addOperation(new OperationNode(HttpMethod.POST, normalized_path_url, pathItem.getPost()));
                    } catch (Exception e) {
                        System.out.println("bug here");
                        throw (e);
                    }
                }
                if (pathItem.getGet() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.GET, normalized_path_url, pathItem.getGet()));
                }
                if (pathItem.getPut() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.PUT, normalized_path_url, pathItem.getPut()));
                }
                if (pathItem.getPatch() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.PATCH, normalized_path_url, pathItem.getPatch()));
                }
                if (pathItem.getOptions() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.OPTIONS, normalized_path_url, pathItem.getOptions()));
                }
                if (pathItem.getTrace() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.TRACE, normalized_path_url, pathItem.getTrace()));
                }
                if (pathItem.getDelete() != null) {
                    operationList.addOperation(new OperationNode(HttpMethod.DELETE, normalized_path_url, pathItem.getDelete()));
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

    public static Components getComponents() {
        return components;
    }

    public static void setComponents(Components components) {
        OpenAPIParser.components = components;
    }

    public static Map<String, Schema> getSchema() {
        return schema;
    }

    public static void setSchema(Map<String, Schema> schema) {
        OpenAPIParser.schema = schema;
    }

}

