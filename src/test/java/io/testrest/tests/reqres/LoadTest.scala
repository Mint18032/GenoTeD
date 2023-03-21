import com.intuit.karate.Runner
import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._

import scala.concurrent.duration.DurationInt

class LoadTest extends Simulation {

   val getOnePerson = scenario("Get a person request").exec(karateFeature("classpath:PerformanceTest.feature@get1"))
   val createOnePerson = scenario("Post, create a new person").exec(karateFeature("classpath:PerformanceTest.feature@create1"))

    // pause times (in milliseconds) per URL pattern and HTTP method.
   val protocol = karateProtocol(
     "/api/users/2" -> pauseFor("get" -> 0),
     "api/users" -> pauseFor("get" -> 0, "post" -> 0)
   )

   // Injects a given number of users with a linear ramp during a given duration.
   // Protocol: Organize your scenario's code and tune the behavior of Gatling's HTTP client.
   setUp(
     getOnePerson.inject(rampUsers(10) during(5 seconds)).protocols(protocol),
     createOnePerson.inject(rampUsers(30) during(10 seconds)).protocols(protocol)
   )

}

// Run:  mvn clean test -P gatling