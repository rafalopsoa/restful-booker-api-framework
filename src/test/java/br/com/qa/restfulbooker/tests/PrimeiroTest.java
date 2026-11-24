package br.com.qa.restfulbooker.tests;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

class PrimeiroTest {
	
	@Test
	void restfulBookerEstaNoAr() {
		given()
				.baseUri("https://restful-booker.herokuapp.com")
			.when()
				.get("/ping")
			.then()
				.statusCode(201);
	}

}
