package com.br.pdvpostocombustivel_frontend.model;

import com.br.pdvpostocombustivel_frontend.model.dto.ContatoResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContatoPageResponse {
    private List<ContatoResponse> content;

    public List<ContatoResponse> getContent() { return content; }
    public void setContent(List<ContatoResponse> content) { this.content = content; }
}