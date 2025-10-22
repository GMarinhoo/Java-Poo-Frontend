package com.br.pdvpostocombustivel_frontend.model;

import com.br.pdvpostocombustivel_frontend.model.dto.EstoqueResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EstoquePageResponse {
    private List<EstoqueResponse> content;

    public List<EstoqueResponse> getContent() { return content; }
    public void setContent(List<EstoqueResponse> content) { this.content = content; }
}