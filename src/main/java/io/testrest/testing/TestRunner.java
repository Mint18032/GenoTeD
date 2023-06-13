package io.testrest.testing;
import com.intuit.karate.Results;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Assertions;

public class TestRunner {
    public TestRunner() {}

    @Karate.Test
    public void testOperation(String testsPath, String operationId) {
        Results results = Karate.run(testsPath).tags("@" + operationId).parallel(0);
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    @Karate.Test
    public void testAll(String path) {
        Results results = Karate.run(path).parallel(5);
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
