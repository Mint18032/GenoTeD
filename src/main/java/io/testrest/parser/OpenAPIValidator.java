package io.testrest.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.testrest.Main;

import java.util.logging.Logger;

public class OpenAPIValidator {
    private static final Logger logger = Logger.getLogger(OpenAPIValidator.class.getName());

    public static void validate(OpenAPI openAPI) throws InvalidOpenAPIException {
        if (openAPI.getServers() == null) {
            throw new InvalidOpenAPIException("No server found.");
        }
        if (openAPI.getPaths() == null) {
            throw new InvalidOpenAPIException("No path found.");
        }

        Main.getConfiguration().setOpenAPIName(openAPI.getInfo().getTitle().replaceAll("\\s+","_").replaceAll("[\\\\/:*?\"<>|]", "_"));
    }

    public static boolean isValidServer(String serverUrl) {
        if (serverUrl.trim().length() <= 4 || !serverUrl.contains("http")) {
            logger.warning("Server url: '" + serverUrl + "' is invalid and will be ignored.");
            return false;
        }

        return true;
    }
}
