package com.br.pdvpostocombustivel_frontend.model.dto;

public record ContatoRequest(
        String telefone,
        String email,
        String endereco
) {}