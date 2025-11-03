package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.PrecoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class PrecoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/precos";

    public PrecoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PrecoResponse getPrecoAtual(Long idProduto) throws HttpClientErrorException {
        String url = API_BASE_URL + "/produto/" + idProduto;
        return restTemplate.getForObject(url, PrecoResponse.class);
    }
}