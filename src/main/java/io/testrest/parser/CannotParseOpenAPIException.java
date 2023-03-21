package io.testrest.parser;

public class CannotParseOpenAPIException extends Exception {
    public CannotParseOpenAPIException() {
    }

    public CannotParseOpenAPIException(String message) {
        super(message);
    }
}
