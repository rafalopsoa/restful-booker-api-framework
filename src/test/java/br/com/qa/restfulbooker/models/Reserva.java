package br.com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Reserva do Restful-booker.Os nomes dos campos seguem o JSON da API (em inglês).
 * Boolean (objeto) em vez de boolean permite omitir o campo em testes negativos.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public record Reserva(
		String firstname,
		String lastname,
		BigDecimal totalprice,
		Boolean depositpaid,
		DatasReserva bookingdates,
		String additionalneeds) {
	
}