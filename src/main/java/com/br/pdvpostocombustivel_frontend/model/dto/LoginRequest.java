package com.br.pdvpostocombustivel_frontend.model.dto;

public record LoginRequest(
        String usuario,
        String senha
) {}