package io.testrest.core.oracle;

import com.intuit.karate.Results;
import io.testrest.datatype.graph.OperationNode;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        if (results.getFeaturesPassed() == results.getFeaturesTotal()) {
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
        System.out.println();
    }
}
