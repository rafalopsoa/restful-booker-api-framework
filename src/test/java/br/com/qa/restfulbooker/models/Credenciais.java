package br.com.qa.restfulbooker.models;

/** Corpo do POST /auth.O toString foi sobrescrito para a senha nunca aparecer em log. */

public record Credenciais(String username, String password) {
	@Override
	public String toString() {
		return "Credenciais[username=" + username + ", password=****]";
	}
}