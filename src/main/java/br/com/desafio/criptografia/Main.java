package br.com.desafio.criptografia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.desafio.DTO.ResultadoDTO;

public class Main {

	private final static String TOKEN = "585bc279d597ab2e0a14acad5705bb2a83b7e610";
	private final static int ASCII_a = 97;
	private final static int ASCII_z = 122;
	private final static String URL_CODENATION = "https://api.codenation.dev/v1/challenge/dev-ps";

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		ResultadoDTO resultado = getRequisicaoInicial();
		criarAtualizaArquivo(resultado);

		resultado.setDecifrado(decifrando(resultado.getCifrado(), resultado.getNumero_casas()));
		criarAtualizaArquivo(resultado);

		resultado.setResumo_criptografico(sha1(resultado.getDecifrado()));
		criarAtualizaArquivo(resultado);

		String response = postFile("answer", fileToByte(getFile()));
		System.out.println("===============> RESPOSTA "+ response);
	}

	private static ResultadoDTO getRequisicaoInicial() {
		RestTemplate client2 = new RestTemplate();
		ResponseEntity<ResultadoDTO> exchange2 = client2.exchange(
				URL_CODENATION + "/generate-data?token=" + TOKEN, HttpMethod.GET, null,
				ResultadoDTO.class);
		return exchange2.getBody();
	}

	public static String postFile(String filename, byte[] someByteArray) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();

		ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name("answer")
				.filename(filename).build();
		fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

		HttpEntity<byte[]> fileEntity = new HttpEntity<>(someByteArray, fileMap);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("answer", fileEntity);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<String> response = 
					restTemplate.exchange(URL_CODENATION + "/submit-solution?token=" + TOKEN, HttpMethod.POST, requestEntity,String.class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void criarAtualizaArquivo(ResultadoDTO dto) throws IOException {
		File file = new File("d://answer.json");

		if (file.createNewFile())
			System.out.println("Arquivo Criado");
		else
			System.out.println("Arquivo ja existe (Será alterado)");

		FileWriter writer = new FileWriter(file);
		writer.write(converetObjetoEmString(dto));
		writer.close();
	}

	private static File getFile() {
		File file = new File("d://answer.json");
		return file;
	}

	private static String converetObjetoEmString(ResultadoDTO dto) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String resultado = mapper.writeValueAsString(dto);
		return resultado;
	}

	private static String decifrando(String string, Integer numeroCasasDecimais) {

		char[] letras = string.toCharArray();
		StringBuilder frase = new StringBuilder();

		for (char let : letras) {
			String letra = String.valueOf(let);
			if (letra.contains(".") || letra.contains(",") || letra.contains(" ") || NumberUtils.isDigits(letra)) {
				frase.append(let);
			} else if ((int) let - numeroCasasDecimais == ASCII_a) {
				frase.append((char) ASCII_a);
			} else if ((int) let - numeroCasasDecimais < ASCII_a) { 
				Integer dif = (int) let - ASCII_a;
				frase.append((char) ((ASCII_z+1) - (numeroCasasDecimais -dif)));
			} else {
				frase.append((char) (((int) let) - numeroCasasDecimais));
			}
		}

		System.out.println(frase.toString());
		return frase.toString();
	}

	private static String sha1(String texto) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(texto.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	private static byte[] fileToByte(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
}
