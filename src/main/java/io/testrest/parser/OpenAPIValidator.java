package io.testrest.parser;

import io.swagger.v3.oas.models.OpenAPI;
import io.testrest.Main;

public class OpenAPIValidator {
    public static void validate(OpenAPI openAPI) throws InvalidOpenAPIException {
        if (openAPI.getServers() == null) {
            throw new InvalidOpenAPIException("No server found.");
        }
        if (openAPI.getPaths() == null) {
            throw new InvalidOpenAPIException("No path found.");
        }
        Main.getConfiguration().setOpenAPIName(openAPI.getInfo().getTitle().replaceAll("\\s+","_").replaceAll("[\\\\/:*?\"<>|]", "_"));
    }
}
