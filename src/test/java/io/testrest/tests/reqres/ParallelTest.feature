Feature: ParallelTest

  Background:
    Given url 'https://reqres.in/api/'

  @delete2
  Scenario: Delete user 2
    Given path 'users/2'
    When method DELETE
    Then status 204
    And print response

  @get2
  Scenario: Get user 2
    Given path 'users/2'
    When method GET
    Then status 200
    And print response

  @delAndGet
  Scenario: Get user 2 after deleting
    Given path 'users/2'
    When method DELETE
    And print response
    Given path 'users/2'
    When method GET
    Then status 404
    And print response

  @getAndDel
  Scenario: Get user 2 then delete
    Given path 'users/2'
    When method GET
    Then status 200
    And print response
    Given path 'users/2'
    When method DELETE
    Then status 204
    And print response