package io.testrest.datatype.parameter;


import com.google.gson.*;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;
import java.util.logging.Logger;

public class ParameterFactory {

    private static final Logger logger = Logger.getLogger(ParameterFactory.class.getName());

    public static ParameterElement getParameterElement(ParameterElement parent, Parameter elementMap, OperationNode operation, String name) {

        checkUnsupportedFeature(elementMap, operation, name);

        // Before the type check, look for combined schemas
        if (!elementMap.getSchema().getAllOf().isEmpty()) {
            return new AllOfParameter(parent, elementMap, operation, name);
        }

        if (!elementMap.getSchema().getAnyOf().isEmpty()) {
            return new AnyOfParameter(parent, elementMap, operation, name);
        }

        if (!elementMap.getSchema().getOneOf().isEmpty()) {
            return new OneOfParameter(parent, elementMap, operation, name);
        }

        // The type can be defined in the element map or in the schema defined in the element map, depending on
        // the kind of the parameter (request body/response body parameter vs. header/path/query/cookie parameter)
        @SuppressWarnings("unchecked")
        Schema targetMap = elementMap.getSchema();
        ParameterType type = ParameterType.getTypeFromString(targetMap.getType());

        switch (type) {
            case ARRAY:
                return new ArrayParameter(parent, elementMap, operation, name);
            case OBJECT:
                return new ObjectParameter(parent, elementMap, operation, name);
            case BOOLEAN:
                return new BooleanParameter(parent, elementMap, operation, name);
            case NUMBER:
            case INTEGER:
                return new NumberParameter(parent, elementMap, operation, name);
            case STRING:
                return new StringParameter(parent, elementMap, operation, name);
            case UNKNOWN:
            default:
                // Fallback
                logger.warning("Unsupported type '" + elementMap.getSchema().getType() + "' for parameter " +
                        name + "(" + operation + "). Created a generic parameter.");
                try {
                    return new GenericParameter(parent, elementMap, operation, name);
                } catch (ClassCastException e) {
                    throw new ParameterCreationException("Unable to create generic parameter");
                }
        }
    }

    public static ParameterElement getParameterElement(ParameterElement parent, Parameter elementMap, OperationNode operation) {
        return getParameterElement(parent, elementMap, operation, parent.getSchemaName());
    }

    public static ParameterElement getParameterElement(ParameterElement parent, JsonElement jsonElement, OperationNode operation, String name) {
        if (jsonElement instanceof JsonObject) {
            return new ObjectParameter((JsonObject) jsonElement, operation, parent, name);
        } else if (jsonElement instanceof JsonArray) {
            return new ArrayParameter((JsonArray) jsonElement, operation, parent, name);
        } else if (jsonElement instanceof JsonPrimitive) {
            JsonPrimitive primitive = (JsonPrimitive) jsonElement;
            if (primitive.isString()) {
                return new StringParameter(operation, parent, name);
            } else if (primitive.isNumber()) {
                return new NumberParameter(primitive, operation, parent, name);
            } else if (primitive.isBoolean()) {
                return new BooleanParameter(operation, parent, name);
            }
        } else if (jsonElement instanceof JsonNull) {
            return new NullParameter(jsonElement, operation, parent, name);
        }

        // Fallback: return null if the jsonElement is not what is expected
        return null;
    }

    public static StructuredParameterElement getStructuredParameter (ParameterElement parent, Parameter elementMap, OperationNode operation, String name) {
        ParameterElement parameter = getParameterElement(parent, elementMap, operation, name);
        try {
            return (StructuredParameterElement) parameter;
        } catch (ClassCastException e) {
            name = getParameterName(elementMap, name);
            throw new ParameterCreationException("Cannot cast to structured parameter " +
                    (name.equals("") ? "" : "'" + name + "' ") +
                    "in operation '" + operation + "'.");
        }
    }

    private static void checkUnsupportedFeature(Parameter elementMap, OperationNode operation, String name) {
        name = elementMap.getName();
//        if (elementMap.containsKey("not")) {
//            throw new UnsupportedSpecificationFeature("Unsupported property 'not' found in " +
//                    (name.equals("") ? "" : "'" + name + "', ") +
//                    "operation '" + operation + "'.");
//        }
    }

    private static String getParameterName(Parameter elementMap, String name) {
        return name != null ? name : elementMap.getName();
    }
}

