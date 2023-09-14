package io.testrest.parser;

public class NoServerUrlFoundException extends NullPointerException {
    public NoServerUrlFoundException() {
        super();
    }
    public NoServerUrlFoundException(String message) {
        super(message);
    }
}
