package io.testrest.core.oracle;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intuit.karate.Results;
import com.intuit.karate.core.StepResult;
import io.testrest.datatype.HttpStatusCode;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.core.testing.TestRunner;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class StatusCodeOracle {
    protected static Logger logger;
    protected HttpStatusCode currentStatus;

    TestRunner testRunner;

    StatusCodeOracle() {
        testRunner = new TestRunner();
    }

    /**
     * @return true if the testcase should be kept.
     */
    public abstract boolean assessOperationTest(OperationNode operationNode, List<String> testPath);

    public void deleteTestcase(String testPath, String operationId) throws IOException {
        File inputFile = new File(testPath);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        boolean pending = false;
        String lineToRemove = "@".concat(operationId);
        String currentLine;

        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.contains(lineToRemove) && !pending) {
                pending = true;
            }
            else if (currentLine.contains(lineToRemove) && pending) {
                sb.append(temp);
                temp.delete(0, temp.length() - 1);
            }

            if (pending)
                temp.append(currentLine).append(System.getProperty("line.separator"));
            else
                sb.append(currentLine).append(System.getProperty("line.separator"));
        }

        reader.close();

        FileWriter fileWriter = new FileWriter(inputFile, false);
        fileWriter.write(sb.toString().replace("\n\n\n", ""));
        fileWriter.close();
    }

    public HttpStatusCode getResponseStatus(String operationId, String scenario, List<String> testPaths) {
        Results results = this.testRunner.testScenario(testPaths.get(0), operationId, scenario);
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
        }

        return new HttpStatusCode(Integer.parseInt(status));
    }

    protected boolean isStatusCode(String str) {
        if (str == null || str.length() != 3) {
            return false;
        }
        try {
            int num = Integer.parseInt(str);
            return num > 0;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public HttpStatusCode getCurrentStatus() {
        return currentStatus;
    }
}
