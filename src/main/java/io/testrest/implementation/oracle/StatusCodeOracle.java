package io.testrest.implementation.oracle;

import io.testrest.datatype.graph.OperationNode;
import io.testrest.testing.TestRunner;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

public abstract class StatusCodeOracle {
    protected static final Logger logger = Logger.getLogger(StatusCodeOracle.class.getName());

    TestRunner testRunner;

    StatusCodeOracle() {
        testRunner = new TestRunner();
    }

    /**
     * @return true if the testcase should be kept.
     */
    public abstract boolean assessOperationTest(OperationNode operationNode, List<String> testPath);

    public void deleteTestcase(String testPath, String operationId) throws IOException { {
        File inputFile = new File(testPath);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        StringBuilder sb = new StringBuilder();
        String lineToRemove = "@".concat(operationId);
        String currentLine;

        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.contains(lineToRemove)) break;
            sb.append(currentLine).append(System.getProperty("line.separator"));
        }

        reader.close();

        FileWriter fileWriter = new FileWriter(inputFile, false);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }
    }
}
