package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AcessoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/acessos";

    public AcessoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AcessoResponse registrar(AcessoRequest request) {
        String url = API_BASE_URL + "/registrar";
        return restTemplate.postForObject(url, request, AcessoResponse.class);
    }
}