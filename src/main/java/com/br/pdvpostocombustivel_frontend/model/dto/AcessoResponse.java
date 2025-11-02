package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;

public record AcessoResponse(
        Long idAcesso,
        String usuario,
        TipoAcesso perfil,
        Long idPessoa,
        String nomePessoa
) {}