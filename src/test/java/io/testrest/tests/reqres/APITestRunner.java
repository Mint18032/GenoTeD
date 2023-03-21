package io.testrest.tests.reqres;

import com.intuit.karate.junit5.Karate;

class APITestRunner {

    @Karate.Test
    Karate testFunction() {
        return Karate.run("FunctionalTest").relativeTo(getClass());
    }

    @Karate.Test
    Karate testPerformance() {
        return Karate.run("PerformanceTest").relativeTo(getClass());
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run().relativeTo(getClass());
    }
}