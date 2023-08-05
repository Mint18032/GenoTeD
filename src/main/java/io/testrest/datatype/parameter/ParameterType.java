package io.testrest.datatype.parameter;


import java.util.logging.Logger;

public enum ParameterType {
    INTEGER,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY,
    STRING,
    // To support combined schemas
    ALLOF,
    ANYOF,
    ONEOF,

    MISSING, // To codify missing type and increase fault tolerance
    UNKNOWN // Unknown type. To increase fault tolerance
    ;

    private static final Logger logger = Logger.getLogger(ParameterType.class.getName());

    public static ParameterType getTypeFromString(String typeName) {
        if (typeName == null) {
            return MISSING;
        }

        switch (typeName.toLowerCase()) {
            case "string":
                return STRING;
            case "integer":
                return INTEGER;
            case "number":
                return NUMBER;
            case "boolean":
                return BOOLEAN;
            case "object":
                return OBJECT;
            case "array":
                return ARRAY;
            case "allof":
                return ALLOF;
            case "anyof":
                return ANYOF;
            case "oneof":
                return ONEOF;
            default:
                logger.warning("Unknown type \"" + typeName + "\".");
                return UNKNOWN;
        }
    }


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}

