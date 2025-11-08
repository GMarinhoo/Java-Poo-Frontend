package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class AcessoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/acessos";

    public AcessoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AcessoResponse> listarAcessos() throws HttpClientErrorException {
        AcessoResponse[] arrayAcessos = restTemplate.getForObject(API_BASE_URL, AcessoResponse[].class);
        if (arrayAcessos != null) {
            return Arrays.asList(arrayAcessos);
        }
        return List.of();
    }

    public void excluirAcesso(Long id) throws HttpClientErrorException {
        String url = API_BASE_URL + "/" + id;
        restTemplate.delete(url);
    }

    public AcessoResponse atualizarPerfil(Long id, TipoAcesso novoPerfil) throws HttpClientErrorException {
        String url = API_BASE_URL + "/" + id + "/perfil";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TipoAcesso> requestEntity = new HttpEntity<>(novoPerfil, headers);

        return restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                requestEntity,
                AcessoResponse.class
        ).getBody();
    }
}