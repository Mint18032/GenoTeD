package io.testrest.datatype.parameter;


import java.util.logging.Logger;

public enum ParameterStyle {
    MATRIX,
    LABEL,
    FORM,
    SIMPLE,
    SPACE_DELIMITED,
    PIPE_DELIMITED,
    DEEP_OBJECT;

    private static final Logger logger = Logger.getLogger(ParameterStyle.class.getName());

    public static ParameterStyle getStyleFromString(String styleName) {
        if (styleName == null) {
            return null;
        }
        switch (styleName.toLowerCase()) {
            case "matrix":
                return MATRIX;
            case "label":
                return LABEL;
            case "form":
                return FORM;
            case "simple":
                return SIMPLE;
            case "spacedelimited":
                return SPACE_DELIMITED;
            case "pipedelimited":
                return PIPE_DELIMITED;
            case "deepobject":
                return DEEP_OBJECT;
            default:
                logger.warning("Unknown type \"" + styleName + "\".");
                return null;
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "");
    }
}

