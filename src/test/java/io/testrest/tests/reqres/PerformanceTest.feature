Feature: PerformanceTest

  Background:
    Given url 'https://reqres.in/'

  @get1
  Scenario: Get a person request
    Given path 'api/users/2'
    When method GET
    Then status 200
    And print response

  @create1
  Scenario: Post, create a new person
    * def user =
     """
    {
      "email": "eve.holt@reqres.in",
      "password": "cityslicka"
    }
     """
    Given path 'api/users'
    And request user
    When method POST
    Then status 201
    And print response