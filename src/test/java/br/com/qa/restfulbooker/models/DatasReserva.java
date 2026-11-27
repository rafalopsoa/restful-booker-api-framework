package br.com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/** Datas como texto (AAAA-MM-DD) para também conseguirmos enviar datas inválidas. */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public record DatasReserva(String checkin, String checkout) {
	
}
