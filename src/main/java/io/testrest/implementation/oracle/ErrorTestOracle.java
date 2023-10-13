package io.testrest.implementation.oracle;

import com.intuit.karate.Results;
import io.testrest.datatype.graph.OperationNode;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorTestOracle extends StatusCodeOracle {
    public ErrorTestOracle() {
        super();
        logger = Logger.getLogger(ErrorTestOracle.class.getName());
    }

    /**
     * @param operationNode the Operation to be tested.
     * @param testPaths paths to the testcases.
     * @return true if testcases are valid.
     */
    @Override
    public boolean assessOperationTest(OperationNode operationNode, List<String> testPaths) {

        return true;
    }
}