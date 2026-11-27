package br.com.qa.restfulbooker.tests;

import br.com.qa.restfulbooker.models.Credenciais;
import br.com.qa.restfulbooker.models.DatasReserva;
import br.com.qa.restfulbooker.models.Reserva;
import br.com.qa.restfulbooker.models.ReservaCriada;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReservaFluxoTest {
	
	private static final String BASE_URI = "https://restful-booker.herokuapp.com";	
	private static String token;
	
	@BeforeAll
	static void obterToken() {
		String senha = System.getenv("RB_SENHA");
		assertNotNull(senha, "Defina a variável de ambiente RB_SENHA");
		
		token = given()
				.baseUri(BASE_URI)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(new Credenciais("admin", senha))
			.when()
				.post("/auth")
			.then()
				.statusCode(200)
			.extract()
				.path("token");
	}
	
	private Reserva novaReserva(String nome) {
			return new Reserva(nome, "Automacao", new BigDecimal("150"), true, 
					new DatasReserva("2027-01-10", "2027-01-13"), "Cafe da Manha");
	}
	
	@Test
	@DisplayName("Criar reserva devolve id e os mesmos dados enviados")
	void criarReserva() {
		Reserva enviada = novaReserva("Fluxo" + System.currentTimeMillis());
		
		ReservaCriada criada = given()
				.baseUri(BASE_URI)
				.contentType(ContentType.JSON)
				.body(enviada)
			.when()
				.post("/booking")
			.then()
				.statusCode(200)
			.extract()
				.as(ReservaCriada.class);
		
		assertAll(
				() -> assertNotNull(criada.bookingid(), "bookingid não veio"),
				() -> assertEquals(enviada.firstname(), criada.booking().firstname()),
				() -> assertEquals(enviada.bookingdates(), criada.booking().bookingdates()));
				
				// Limpeza: por enquanto manual.
				
		given()
				.baseUri(BASE_URI)
				.accept("application/json")
				.cookie("token", token)
				.pathParam("id", criada.bookingid())
				
			.when()
				.delete("/booking/{id}");
		
	}
	
	@Test
	@DisplayName("Fluxo completo: criar, PUT, PATCH, DELETE e consultar novamente")
	void fluxoCompleto() {
		int id = given()
				.baseUri(BASE_URI)
				.contentType(ContentType.JSON)
				.accept("application/json")
				.body(novaReserva("Fluxo" + System.currentTimeMillis()))
			.when()
				.post("/booking")
			.then()
				.statusCode(200)
			.extract()
				.path("bookingid");
		
		Reserva alterada = novaReserva("Alterada" + System.currentTimeMillis());
		
		// PUT: substitui a reserva inteira
		given()
				.baseUri(BASE_URI)
				.contentType(ContentType.JSON)
				.accept("application/json")
				.cookie("token", token)
				.pathParam("id", id)
				.body(alterada)
			.when()
				.put("/booking/{id}")
			.then()
				.statusCode(200)
				.body("firstname", equalTo(alterada.firstname()));
		
		given()
				.baseUri(BASE_URI)
				.contentType(ContentType.JSON)
				.accept("application/json")
				.cookie("token", token)
				.pathParam("id", id)
				.body(Map.of("lastname", "Parcial"))
				
			.when()
				.patch("/booking/{id}")
			.then()
				.statusCode(200)
				.body("lastname", equalTo("Parcial"))
				.body("firstname", equalTo(alterada.firstname()));
		
		given()
				.baseUri(BASE_URI)
				.accept("application/json")
				.cookie("token", token)
				.pathParam("id", id)
			.when()
				.delete("/booking/{id}")
			.then()
				.statusCode(201);
		
		given()
				.baseUri(BASE_URI)
				.accept("application/json")
				.pathParam("id", id)
			.when()
				.get("/booking/{id}")
			.then()
				.statusCode(404);
	}
}
