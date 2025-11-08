package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.EstoquePageResponse;
import com.br.pdvpostocombustivel_frontend.model.dto.EstoqueRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.EstoqueResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
public class EstoqueService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/estoques";

    public EstoqueService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<EstoqueResponse> listarEstoques() {
        EstoquePageResponse pageResponse = restTemplate.getForObject(API_BASE_URL, EstoquePageResponse.class);
        return pageResponse.getContent();
    }

    public EstoqueResponse salvarEstoque(EstoqueRequest estoqueRequest, Long id) {
        if (id == null) {
            return restTemplate.postForObject(API_BASE_URL, estoqueRequest, EstoqueResponse.class);
        } else {
            restTemplate.put(API_BASE_URL + "/" + id, estoqueRequest);

            return new EstoqueResponse(
                    id,
                    estoqueRequest.idProduto(),
                    estoqueRequest.quantidade(),
                    estoqueRequest.localTanque(),
                    estoqueRequest.loteEndereco(),
                    estoqueRequest.loteFabricacao(),
                    estoqueRequest.dataValidade(),
                    estoqueRequest.tipo()
            );
        }
    }

    public void excluirEstoque(Long id) {
        restTemplate.delete(API_BASE_URL + "/" + id);
    }
}