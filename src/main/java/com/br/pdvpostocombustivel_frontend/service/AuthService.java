package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.AcessoResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.LoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final String API_AUTH_URL = "http://localhost:8080/api/v1/auth";

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AcessoResponse login(LoginRequest loginRequest) throws HttpClientErrorException {
        return restTemplate.postForObject(API_AUTH_URL + "/login", loginRequest, AcessoResponse.class);
    }
}