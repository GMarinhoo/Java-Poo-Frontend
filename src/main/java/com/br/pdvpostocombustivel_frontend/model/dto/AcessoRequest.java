package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;

public record AcessoRequest(
        String usuario,
        String senha,
        TipoAcesso perfil
) {}