package io.testrest.core.testing;
import com.intuit.karate.Results;
import com.intuit.karate.junit5.Karate;
import io.testrest.Environment;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class TestRunner {

    public TestRunner() {
        System.setProperty("karate.options", "-o \"" + Environment.getConfiguration().getOutputPath() + "/NominalTests\"" );
    }

    @Karate.Test
    public Results testOperation(String testPath, String operationId) {
        return Karate.run(testPath).outputHtmlReport(false).outputCucumberJson(false).outputJunitXml(false).tags("@" + operationId).parallel(0);
    }

    /**
     * Run all tests.
     * @param paths all nominal and error test paths from all servers.
     */
    @Karate.Test
    public void testAll(List<String> paths) {
        Results results = Karate.run(paths.toArray(new String[0])).parallel(10); //.reportDir(path.substring(0, path.lastIndexOf("/")))
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    /**
     * Run all tests in a feature file.
     * @param path to the feature.
     */
    @Karate.Test
    public void testAll(String path) {
        Results results = Karate.run(path).parallel(5); //.reportDir(path.substring(0, path.lastIndexOf("/")))
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
