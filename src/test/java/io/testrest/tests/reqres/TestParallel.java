package io.testrest.tests.reqres;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Using Junit 5
 * Report can be customized using Cucumber
 */
public class TestParallel {

    @Test
    public void testParallel() {
        Results results = Runner.path("classpath:ParallelTest").parallel(15);
        assertTrue(results.getErrorMessages(), results.getFailCount() == 0);
    }

}
