package br.com.qa.restfulbooker.tests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExploracaoTest {
	
	private static final String BASE_URI = "https://restful-booker.herokuapp.com";
	
	@Test
	@DisplayName("GET /ping responde 201 em menos de 2 segundos")
	void pingResponde() {
		given()
				.baseUri(BASE_URI)
			.when()
				.get("/ping")
			.then()
				.log().all()
				.statusCode(201)
				.time(lessThan(2000L));
	}
	
	@Test
	@DisplayName("GET /booking devolve uma lista JSON de ids")
	void listarIds() {
		given()
				.baseUri(BASE_URI)
				.accept(ContentType.JSON)
			.when()
				.get("/booking")
			.then()
				.log().all()
				.statusCode(200)
			.extract()
				.jsonPath().getList("bookingid", Integer.class);
	}
	
	@Test
	@DisplayName("GET /booking/{id} devolve uma reserva com os campos principais")
	void buscarPrimeiraReserva() {
		List<Integer> ids = given()
				.baseUri(BASE_URI)
				.accept(ContentType.JSON)
			.when()
				.get("/booking")
			.then()
				.statusCode(200)
			.extract()
				.jsonPath().getList("bookingid", Integer.class);
		
		assertFalse(ids.isEmpty(), "Não há reservas. Crie uma no Postman antes de executar esse teste");
		int primeiroId = ids.get(0);
		
		given()
				.baseUri(BASE_URI)
				.accept("application/json")
				.pathParam("id", primeiroId)
				.log().all()
			.when()
				.get("/booking/{id}")
			.then()
				.log().all()
				.statusCode(200)
				.body("firstname", notNullValue())
				.body("depositpaid", notNullValue())
				.body("bookingdates.checkin", matchesPattern("\\d{4}-\\d{2}-\\d{2}"))
				.body("bookingdates.checkout", matchesPattern("\\d{4}-\\d{2}-\\d{2}"));
	}
	
	@Test
	@DisplayName("GET /booking/{id} com o Accept padrão (*/*) responde 200 [observado]")
	void buscarComAcceptPadrao() {
	    int id = primeiroId();

	    given()
	            .baseUri(BASE_URI)
	            .pathParam("id", id)
	        .when()
	            .get("/booking/{id}")
	        .then()
	            .statusCode(200);
	}

	@Test
	@DisplayName("GET /booking/{id} com Accept em lista responde 418 [observado; o HTTP permite lista]")
	void buscarComAcceptEmLista() {
	    int id = primeiroId();

	    given()
	            .baseUri(BASE_URI)
	            .accept(ContentType.JSON)   // o Rest Assured envia uma LISTA de tipos JSON
	            .pathParam("id", id)
	        .when()
	            .get("/booking/{id}")
	        .then()
	            .statusCode(418);
	}

	private int primeiroId() {
	    return given()
	            .baseUri(BASE_URI)
	            .accept("application/json")
	        .when()
	            .get("/booking")
	        .then()
	            .statusCode(200)
	        .extract()
	            .path("[0].bookingid");
	}
	
	@Test
	@DisplayName("GET /booking/{id} inexistente responde 404")
	void buscarInexistente() {
		given()
				.baseUri(BASE_URI)
				.accept(ContentType.JSON)
				.pathParam("id", 999_999_999)
			.when()
				.get("/booking/{id}")
			.then()
				.statusCode(404);
	}

}
