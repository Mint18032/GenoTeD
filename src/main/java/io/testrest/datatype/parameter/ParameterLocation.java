package io.testrest.datatype.parameter;

import java.util.logging.Logger;

public enum ParameterLocation {
    HEADER,
    PATH,
    QUERY,
    COOKIE,
    REQUEST_BODY,
    RESPONSE_BODY,

    MISSING,
    UNKNOWN
    ;

    private static final Logger logger = Logger.getLogger(ParameterLocation.class.getName());

    public static ParameterLocation getLocationFromString(String location) {
        if (location == null) {
            return MISSING;
        }
        switch (location.toLowerCase()) {
            case "header":
                return HEADER;
            case "path":
                return PATH;
            case "query":
                return QUERY;
            case "cookie":
                return COOKIE;
            case "request_body":
                return REQUEST_BODY;
            case "response_body":
                return RESPONSE_BODY;
            default:
                logger.warning("Unsupported location \"" + location + "\" for parameters.");
                return UNKNOWN;
        }
    }
}

