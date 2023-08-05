Feature: Basic Nominal tests

	Background:
		Given url 'https://6-dot-authentiqio.appspot.com'
		* configure headers = { Accept: 'application/json' }

	@key_register
	Scenario: key_register
		Given path '/key'
		When method POST
		Then status 201
		And print response

	@key_revoke_nosecret
	Scenario: key_revoke_nosecret
		Given path '/key'
		And param email = "zpn@live.com"
		And param phone = "MZC0"
		And param code = "overholy"
		When method DELETE
		Then status 200
		And print response

	@null
	Scenario: null
		Given path '/key/', 'unsuccessiveness'
		When method HEAD
		Then status 200
		And print response

	@key_update
	Scenario: key_update
		Given path '/key/', 'unsuccessiveness'
		When method POST
		Then status 200
		And print response

	@key_retrieve
	Scenario: key_retrieve
		Given path '/key/', 'unsuccessiveness'
		When method GET
		Then status 200
		And print response

	@key_bind
	Scenario: key_bind
		Given path '/key/', 'unsuccessiveness'
		When method PUT
		Then status 200
		And print response

	@key_revoke
	Scenario: key_revoke
		Given path '/key/', 'unsuccessiveness'
		And param secret = "NV"
		When method DELETE
		Then status 200
		And print response

	@push_login_request
	Scenario: push_login_request
		Given path '/login'
		And param callback = "FW"
		When method POST
		Then status 200
		And print response

	@sign_request
	Scenario: sign_request
		Given path '/scope'
		And param test = "89"
		When method POST
		Then status 201
		And print response

	@sign_retrieve_head
	Scenario: sign_retrieve_head
		Given path '/scope/', '80692b4bab33b13f7'
		When method HEAD
		Then status 200
		And print response

	@sign_confirm
	Scenario: sign_confirm
		Given path '/scope/', '80692b4bab33b13f7'
		When method POST
		Then status 202
		And print response

	@sign_retrieve
	Scenario: sign_retrieve
		Given path '/scope/', '80692b4bab33b13f7'
		When method GET
		Then status 200
		And print response

	@sign_update
	Scenario: sign_update
		Given path '/scope/', '80692b4bab33b13f7'
		When method PUT
		Then status 200
		And print response

	@sign_delete
	Scenario: sign_delete
		Given path '/scope/', '80692b4bab33b13f7'
		When method DELETE
		Then status 200
		And print response