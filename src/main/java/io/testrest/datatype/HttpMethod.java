package io.testrest.datatype;

public enum HttpMethod {
    HEAD,
    POST,
    GET,
    PUT,
    PATCH,
    OPTIONS,
    TRACE,
    DELETE;

    public static HttpMethod getMethod(String stringMethod) {
        for (HttpMethod method : HttpMethod.values()) {
            if (method.name().equalsIgnoreCase(stringMethod)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid value '" + stringMethod + "' for HTTP method.");
    }
}
