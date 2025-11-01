package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.LoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final RestTemplate restTemplate;

    private final String AUTH_URL = "http://localhost:8080/api/v1/auth";
    private final String ACESSO_URL = "http://localhost:8080/api/v1/acessos";

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AcessoResponse login(LoginRequest request) throws HttpClientErrorException {
        String url = AUTH_URL + "/login";
        return restTemplate.postForObject(url, request, AcessoResponse.class);
    }

    public AcessoResponse registrar(AcessoRequest request) throws HttpClientErrorException {
        String url = ACESSO_URL + "/registrar";
        return restTemplate.postForObject(url, request, AcessoResponse.class);
    }
}