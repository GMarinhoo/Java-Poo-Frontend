package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.CustoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.CustoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CustoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/custos";

    public CustoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<CustoResponse> listarCustos() throws HttpClientErrorException {
        CustoResponse[] arrayCustos = restTemplate.getForObject(API_BASE_URL, CustoResponse[].class);
        if (arrayCustos != null) {
            return Arrays.asList(arrayCustos);
        }
        return List.of();
    }

    public CustoResponse salvarCusto(CustoRequest request, Long id) throws HttpClientErrorException {
        if (id == null) {
            return restTemplate.postForObject(API_BASE_URL, request, CustoResponse.class);
        } else {
            String url = API_BASE_URL + "/" + id;
            restTemplate.put(url, request);

            return restTemplate.getForObject(url, CustoResponse.class);
        }
    }

    public void excluirCusto(Long id) throws HttpClientErrorException {
        String url = API_BASE_URL + "/" + id;
        restTemplate.delete(url);
    }

    public CustoResponse getCustoMaisRecente() throws HttpClientErrorException {
        List<CustoResponse> todosOsCustos = listarCustos();

        if (todosOsCustos == null || todosOsCustos.isEmpty()) {
            throw new HttpClientErrorException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Nenhum Custo cadastrado. Cadastre um Custo primeiro."
            );
        }

        CustoResponse maisRecente = todosOsCustos.stream()
                .max(java.util.Comparator.comparing(CustoResponse::dataProcessamento))
                .orElse(null);

        return maisRecente;
    }
}