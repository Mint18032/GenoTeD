Feature: FunctionalTest

  Background:
    Given url 'https://reqres.in/'

  @registerPass
  Scenario: Register - Successful
    * def user =
   """
  {
    "email": "eve.holt@reqres.in",
    "password": "pistol"
  }
   """
    Given path 'api/register'
    And request user
    When method POST
    Then status 200


  @registerFail
  Scenario: Register no password - unsuccessful
    * def user =
   """
  {
    "email": "sydney@fife"
  }
   """
    Given path 'api/register'
    And request user
    When method POST
    Then status 400
    And response.error == "Missing password"

  @loginPass
  Scenario: Login - successful
    * def user =
   """
  {
    "email": "eve.holt@reqres.in",
    "password": "cityslicka"
  }
   """
    Given path 'api/login'
    And request user
    When method POST
    Then status 200

  @loginFail
  Scenario: Login no password - unsuccessful
    * def user =
   """
  {
    "email": "peter@klaven"
  }
   """
    Given path 'api/login'
    And request user
    When method POST
    Then status 400
    And response.error == "Missing password"

  @update
  Scenario: Update person - successful
    * def user =
   """
  {
    "name": "morpheus",
    "job": "zion resident"
  }
   """
    Given path 'api/users/2'
    And request user
    When method PUT
    Then status 200



