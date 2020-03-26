package br.com.desafio.DTO;


import lombok.Data;

@Data
public class ResultadoDTO {

	private Integer numero_casas;
	private String token;
	private String cifrado;
	private String decifrado;
	private String resumo_criptografico;
}
