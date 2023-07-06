package io.testrest.testing;
import com.intuit.karate.Results;
import com.intuit.karate.junit5.Karate;
import io.testrest.Environment;
import org.junit.jupiter.api.Assertions;

public class TestRunner {
    private enum TestType {
        NominalTests,
        ErrorTests
    }

    public TestRunner() {
        System.setProperty("karate.options", "-o \"" + Environment.getConfiguration().getOutputPath() + "/NominalTests\"" );
    }

    @Karate.Test
    public void testOperation(String testsPath, String operationId) {
        Results results = Karate.run(testsPath).reportDir(testsPath.substring(testsPath.lastIndexOf("/"))).tags("@" + operationId).parallel(0);
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    @Karate.Test
    public void testAll(String path) {
        Results results = Karate.run(path).parallel(5);
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
