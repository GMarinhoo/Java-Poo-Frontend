package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.VendaRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.VendaResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class VendaService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/vendas";

    public VendaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public VendaResponse registrarVenda(VendaRequest request) throws HttpClientErrorException {
        return restTemplate.postForObject(API_BASE_URL, request, VendaResponse.class);
    }
}