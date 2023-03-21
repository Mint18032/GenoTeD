package io.testrest.parser;

public class InvalidOpenAPIException extends RuntimeException {

    public InvalidOpenAPIException() {
    }

    public InvalidOpenAPIException(String message) {
        super(message);
    }
}
