package io.testrest.testing;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.junit5.Karate;
import io.testrest.Environment;
import org.junit.jupiter.api.Assertions;

import java.util.Map;
import java.util.stream.Collectors;

public class TestRunner {
    private enum TestType {
        NominalTests,
        ErrorTests
    }

    public TestRunner() {
        System.setProperty("karate.options", "-o \"" + Environment.getConfiguration().getOutputPath() + "/NominalTests\"" );
    }

    @Karate.Test
    public Results testOperation(String testPath, String operationId) {
        Results results = Karate.run(testPath).outputHtmlReport(false).tags("@" + operationId).parallel(0);
        return results;
    }

    @Karate.Test
    public void testAll(String path) {
        Results results = Karate.run(path).parallel(5); //.reportDir(path.substring(0, path.lastIndexOf("/")))
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
