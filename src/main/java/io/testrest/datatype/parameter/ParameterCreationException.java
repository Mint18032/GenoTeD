package io.testrest.datatype.parameter;

public class ParameterCreationException extends RuntimeException {

    public ParameterCreationException() {
        super("Unable to create parameter");
    }

    public ParameterCreationException(String description) {
        super(description);
    }
}

