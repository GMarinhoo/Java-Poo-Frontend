package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.PrecoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.PrecoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

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

    public List<PrecoResponse> listarPrecos() throws HttpClientErrorException {
        PrecoResponse[] arrayPrecos = restTemplate.getForObject(API_BASE_URL, PrecoResponse[].class);
        if (arrayPrecos != null) {
            return Arrays.asList(arrayPrecos);
        }
        return List.of();
    }

    public PrecoResponse salvarPreco(PrecoRequest request, Long id) throws HttpClientErrorException {
        if (id == null) {
            return restTemplate.postForObject(API_BASE_URL, request, PrecoResponse.class);
        } else {
            String url = API_BASE_URL + "/" + id;
            restTemplate.put(url, request);

            return restTemplate.getForObject(url, PrecoResponse.class);
        }
    }

    public void excluirPreco(Long id) throws HttpClientErrorException {
        String url = API_BASE_URL + "/" + id;
        restTemplate.delete(url);
    }
}