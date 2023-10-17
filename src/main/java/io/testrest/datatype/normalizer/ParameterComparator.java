package io.testrest.datatype.normalizer;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.datatype.graph.OperationNode;

public class ParameterComparator extends Normalizer {
    /**
     * Normalizes parameter name follows 2 schemes: idCompletion and PorterStemmer.
     *
     * @param parameter an operation's parameter.
     * @return name after being normalized.
     */
    public static String normalize(OperationNode operation, Parameter parameter) {
        return PorterStemming(idCompletion(operation, parameter));
    }

    /**
     * Matches  2 parameters' names by comparing their stemming results. The comparison is case-insensitive.
     * @param p1 first parameter.
     * @param p2 second parameter.
     * @return true if the 2 names are considered the same.
     */
    public static boolean matchedNames(OperationNode o1, Parameter p1, OperationNode o2, Parameter p2) {
        if (!ofSameType(p1, p2)) {
            return false;
        }
        String p1_name = PorterStemming(idCompletion(o1, p1));
        String p2_name = PorterStemming(idCompletion(o2, p2));
        return p1_name.equalsIgnoreCase(p2_name);
    }

    public static boolean matchedNames(OperationNode o1, Parameter p1, String p2) {
        String p1_name = PorterStemming(idCompletion(o1, p1));
        String p2_name = PorterStemming(p2);
        return p1_name.equalsIgnoreCase(p2_name);
    }

    /**
     * ID completion: add prefix to a field named <i>id</i><br/>
     *   - If it is a field of a structured
     * object, the prefix is the name of the object. E.g., the
     * field *id* of the object pet is renamed *petId*.<br/>
     *   - If this field is not part of a structured object, it
     * is prefixed with the name of the operation in which
     * it is involved, after removing get/set verbs from the operation name.
     *
     * @param parameter an operation's parameter.
     * @return name after completion.
     */
    public static String idCompletion(OperationNode operation, Parameter parameter) {
        String name = parameter.getName().trim();
        if (name.equalsIgnoreCase("id")) {
            // TODO: implement if1
            try {
                String path = operation.getPath();
                if (parameter.getIn().equals("path")) {
                    name = path.substring(0, path.indexOf("/{" + name + "}"));
                    name = name.substring(name.lastIndexOf("/") + 1) + "Id";
                } else if (parameter.getIn().equals("query") || parameter.getIn().equals("head")) {
                    if (path.contains("{")) {
                        name = path.substring(0, path.indexOf("/{"));
                        name = name.substring(name.lastIndexOf("/") + 1) + "Id";
                    } else {
                        name = path.substring(path.lastIndexOf("/") + 1) + "Id";
                    }
                }
                if (name.indexOf("get") == 0 || name.indexOf("set") == 0) {
                    name = name.substring(3);
                }
//                parameter.setName(name);
//                System.out.println("Parameter: " + parameter.getName() + " is renamed to: " + name);
            } catch (Exception e) {
                System.out.println("Parameter: " + parameter.getName() + " cannot be renamed due to unsupported OpenAPI Specification.");
            }
        }
        return name;
    }

    /**
     * Uses Porter Stemming algorithm to each parameter name to compare. E.g, pet & pets are considered the same as the root pet.
     *
     * @param name field name.
     * @return Porter Stemming algorithm result.
     */
    public static String PorterStemming(String name) {
        // split String by Capital letters
        String[] strArray = name.split("(?=\\p{Lu})");

        // stemming substrings
        String stemmed_name = "";
        PorterStemmer porterStemmer = new PorterStemmer();
        for (String s : strArray){
            stemmed_name += porterStemmer.stemWord(s);
        }

//        System.out.println(name + " stemmed to " + stemmed_name);
        return stemmed_name;
    }

    public static boolean ofSameType(Parameter p1, Parameter p2) {
        return p1.getSchema().getType().equals(p2.getSchema().getType());
    }
}
