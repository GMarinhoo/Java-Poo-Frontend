package com.br.pdvpostocombustivel_frontend.model.dto;

public record ContatoResponse(
        Long id,
        String telefone,
        String email,
        String endereco
) {}