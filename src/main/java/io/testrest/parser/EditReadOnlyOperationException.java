package io.testrest.parser;

import io.testrest.datatype.graph.OperationNode;

public class EditReadOnlyOperationException extends RuntimeException {

    public EditReadOnlyOperationException(OperationNode o) {
        super("Modifications on read-only instance of operation '" + o.toString() + "' is forbidden.");
    }
}
