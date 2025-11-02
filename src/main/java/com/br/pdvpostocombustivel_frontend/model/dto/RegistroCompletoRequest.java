package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoAcesso;
import com.br.pdvpostocombustivel_frontend.model.enums.TipoPessoa;
import java.time.LocalDate;

public record RegistroCompletoRequest(
        // Campos da Pessoa
        String nomeCompleto,
        String cpfCnpj,
        Long numeroCtps,
        LocalDate dataNascimento,
        TipoPessoa tipoPessoa,


        String usuario,
        String senha,
        TipoAcesso perfil
) {}