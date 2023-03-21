package io.testrest.parser;

public class CannotParseOperationException extends ClassCastException {
    public CannotParseOperationException() {
    }

    public CannotParseOperationException(String s) {
        super(s);
    }
}
