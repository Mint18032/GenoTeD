package io.testrest.core.oracle;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intuit.karate.Results;
import com.intuit.karate.core.StepResult;
import io.testrest.Main;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.datatype.HttpStatusCode;
import io.testrest.datatype.graph.OperationNode;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NominalTestOracle extends StatusCodeOracle {

    public NominalTestOracle() {
        super();
        logger = Logger.getLogger(NominalTestOracle.class.getName());
    }

    /**
     * Deletes 4xx testcases and saves returned values of other testcases to Dictionary.
     * @param operationNode the Operation to be tested.
     * @param testPaths paths to the testcases.
     * @return true if testcases can be used to mutate.
     */
    @Override
    public boolean assessOperationTest(OperationNode operationNode, List<String> testPaths) {
        Results results = this.testRunner.testOperation(testPaths.get(0), operationNode.getOperationId());
        boolean delete = false;

        if (results.getErrors().size() > 0) {
            String error = results.getErrors().get(0);
            String status = error.substring(error.indexOf("status code was: ") + 17, error.indexOf("status code was: ") + 20);

            if (status.equals("411") && addLength(operationNode.getOperationId(), testPaths)) {
                results = this.testRunner.testOperation(testPaths.get(0), operationNode.getOperationId());
                if (results.getErrors().size() > 0) {
                    error = results.getErrors().get(0);
                    status = error.substring(error.indexOf("status code was: ") + 17, error.indexOf("status code was: ") + 20);
                }
            }
            if (status.startsWith("4") || error.contains("Unexpected token") || (!status.startsWith("5") && results.getFeaturesPassed() == 0)) {
                delete = true;
            }
        }

        if (!delete && results.getFeaturesPassed() == results.getFeaturesTotal() && results.getFeaturesPassed() > 0) {
            delete = !receiveResponseValues(operationNode, results, testPaths);
        }

        if (delete) {
            testPaths.forEach(path -> {
                try {
                    deleteTestcase(path, operationNode.getOperationId());
                } catch (IOException e) {
                    logger.warning("Exception raised when attempting to delete testcase for Operation " + operationNode.getOperationId());
                    logger.log(Level.INFO, e.getMessage());
                }
            });

            logger.info("Deleted testcase returning 4xx status code for Operation: " + operationNode.getOperationId());
        }

        return !delete;
    }

    /**
     * Gets values returned by a request and saves them to Dictionary.
     * @param results karate results object.
     * @return true if response value received successfully (status 2xx or 5xx).
     */
    public boolean receiveResponseValues(OperationNode operationNode, Results results, List<String> testPaths) {
        System.out.println("RESPONSE: \n");
        List<StepResult> stepResults = results.getScenarioResults().collect(Collectors.toList()).get(0).getStepResults();
        StepResult print_step = stepResults.get(stepResults.size() - 1);
        String response = print_step.getStepLog();
        response = response.substring(response.lastIndexOf("[print] ") + 8, response.length() - 2).strip();
        System.out.println(response);
        String status = "200";

        if (response.startsWith("{")) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();

            Map<String, Object> map = new HashMap<>(gson.fromJson(response, type));

            if (map.containsKey("code") && isStatusCode(map.get("code").toString())) {
                status = map.get("code").toString();
            } else if (map.containsKey("status") && isStatusCode(map.get("status").toString())) {
                status = map.get("status").toString();
            }

            if (status.startsWith("2")) {
//                Parse response
                List<String> outputs = operationNode.getOutputs();
                outputs.forEach(output -> {
                    Object value = findValueByKey(map, output);
                    if (value != null) {
                        System.out.println("Key: " + output + ", Value: " + value);
                        Main.getEnvironment().getGlobalDictionary().addEntry(new DictionaryEntry(output, value.toString()));
                    }
                });
            }

        } else if (response.startsWith("<") && response.endsWith(">")) {
            if (response.contains("Length Required") && response.contains("411")) {
                status = "411";
                if (addLength(operationNode.getOperationId(), testPaths)) {
                    results = this.testRunner.testOperation(testPaths.get(0), operationNode.getOperationId());
                    status = receiveResponseValues(operationNode, results, testPaths) ? "200" : "400";
                }
            }
        }

        currentStatus = new HttpStatusCode(Integer.parseInt(status));
        return status.startsWith("2") || status.startsWith("5");
    }

    private static Object findValueByKey(Map<String, Object> map, String key) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            } else if (entry.getValue() instanceof Map) {
                Object nestedValue = findValueByKey((Map<String, Object>) entry.getValue(), key);
                if (nestedValue != null) {
                    return nestedValue;
                }
            }
        }
        return null;
    }

    private boolean addLength(String operationId, List<String> testPaths) {
        System.out.println("Adding content-length to request.");
        for (String testPath : testPaths) {
            try {
                File inputFile = new File(testPath);
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));

                StringBuilder sb = new StringBuilder();
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    sb.append(currentLine).append(System.getProperty("line.separator"));
                    if (currentLine.contains("Scenario: " + operationId)) {
                        sb.append("\t\tGiven header content-length = 0").append(System.getProperty("line.separator"));
                    }
                }

                reader.close();

                FileWriter fileWriter = new FileWriter(inputFile, false);
                fileWriter.write(sb.toString());
                fileWriter.close();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

}
