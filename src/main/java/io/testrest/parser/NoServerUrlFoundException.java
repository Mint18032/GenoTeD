package io.testrest.parser;

import javax.mail.Message;

public class NoServerUrlFoundException extends NullPointerException {
    public NoServerUrlFoundException() {
        super();
    }
    public NoServerUrlFoundException(String message) {
        super(message);
    }
}
