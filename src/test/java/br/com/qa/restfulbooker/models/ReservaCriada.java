package br.com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Resposta do POST /booking: o id gerado e a reserva gravada. */

@JsonIgnoreProperties(ignoreUnknown = true)

public record ReservaCriada(Integer bookingid, Reserva booking) {
	
}