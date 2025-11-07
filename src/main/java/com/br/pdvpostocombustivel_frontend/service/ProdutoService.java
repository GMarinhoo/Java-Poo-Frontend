package com.br.pdvpostocombustivel_frontend.service;


import com.br.pdvpostocombustivel_frontend.model.ProdutoPageResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.ProdutoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
public class ProdutoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/produtos";

    public ProdutoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ProdutoResponse> listarProdutos() {
        ProdutoPageResponse pageResponse = restTemplate.getForObject(API_BASE_URL, ProdutoPageResponse.class);
        return pageResponse.getContent();
    }

    public ProdutoResponse salvarProduto(ProdutoRequest produtoRequest, Long id) {
        if (id == null) {
            return restTemplate.postForObject(API_BASE_URL, produtoRequest, ProdutoResponse.class);
        } else {
            restTemplate.put(API_BASE_URL + "/" + id, produtoRequest);
            return new ProdutoResponse(id, produtoRequest.nome(), produtoRequest.referencia(),
                    produtoRequest.fornecedor(), produtoRequest.categoria(), produtoRequest.marca(),
                    produtoRequest.tipoProduto());
        }
    }

    public void excluirProduto(Long id) {
        restTemplate.delete(API_BASE_URL + "/" + id);
    }
}