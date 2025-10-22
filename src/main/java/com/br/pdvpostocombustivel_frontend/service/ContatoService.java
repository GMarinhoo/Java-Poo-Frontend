package com.br.pdvpostocombustivel_frontend.service;

import com.br.pdvpostocombustivel_frontend.model.dto.ContatoRequest;
import com.br.pdvpostocombustivel_frontend.model.dto.ContatoResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
public class ContatoService {

    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api/v1/contatos";

    public ContatoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- MÉTODO CORRIGIDO ---
    public List<ContatoResponse> listarContatos() {
        ContatoResponse[] contatosArray = restTemplate.getForObject(API_BASE_URL, ContatoResponse[].class);
        return contatosArray != null ? Arrays.asList(contatosArray) : List.of();
    }

    public ContatoResponse salvarContato(ContatoRequest contatoRequest, Long id) {
        if (id == null) {
            return restTemplate.postForObject(API_BASE_URL, contatoRequest, ContatoResponse.class);
        } else {
            restTemplate.put(API_BASE_URL + "/" + id, contatoRequest);
            return new ContatoResponse(id, contatoRequest.telefone(), contatoRequest.email(), contatoRequest.endereco());
        }
    }

    public void excluirContato(Long id) {
        restTemplate.delete(API_BASE_URL + "/" + id);
    }
}