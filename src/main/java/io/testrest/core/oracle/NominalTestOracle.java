package io.testrest.core.oracle;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intuit.karate.Results;
import com.intuit.karate.core.StepResult;
import io.testrest.Environment;
import io.testrest.core.dictionary.DictionaryEntry;
import io.testrest.datatype.graph.OperationNode;

import java.io.IOException;
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
     * @return true if testcases are valid.
     */
    @Override
    public boolean assessOperationTest(OperationNode operationNode, List<String> testPaths) {
        Results results = this.testRunner.testOperation(testPaths.get(0), operationNode.getOperationId());

        if (results.getErrors().size() > 0) {
            String error = results.getErrors().get(0);
            String status = error.substring(error.indexOf("status code was: ") + 17, error.indexOf("status code was: ") + 20);

            if (status.startsWith("4") || error.contains("Unexpected token")) {
                testPaths.forEach(path -> {
                            try {
                                deleteTestcase(path, operationNode.getOperationId());
                            } catch (IOException e) {
                                logger.warning("Exception raised when attempting to delete testcase for Operation " + operationNode.getOperationId());
                                logger.log(Level.INFO, e.getMessage());
                            }
                        });
                logger.info("Deleted testcase returning 4xx status code for Operation: " + operationNode.getOperationId());
                return false;
            }
        }

        if (results.getFeaturesPassed() == results.getFeaturesTotal() && results.getFeaturesPassed() > 0) {
            receiveResponseValues(results);
        }

        return true;
    }

    /**
     * Gets values returned by a request and saves them to Dictionary.
     * @param results karate results object.
     */
    public void receiveResponseValues(Results results) {
        System.out.println("+++++++++++++++++++++++++++++++++++\n");
        List<StepResult> stepResults = results.getScenarioResults().collect(Collectors.toList()).get(0).getStepResults();
        StepResult print_step = stepResults.get(stepResults.size() - 1);
        String response = print_step.getStepLog();
        response = response.substring(response.lastIndexOf("[print] ") + 8, response.length() - 2);
        System.out.println(response);

        if (response.startsWith("{") && response.endsWith("}")) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();

            Map<Object, Object> map = new HashMap<>(gson.fromJson(response, type));

            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                Environment.getInstance().getGlobalDictionary().addEntry(new DictionaryEntry(entry.getKey().toString(), entry.getValue()));
            }
        }
    }
}
