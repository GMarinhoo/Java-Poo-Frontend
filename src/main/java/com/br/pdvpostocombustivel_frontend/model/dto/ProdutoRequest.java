package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoProduto;

public record ProdutoRequest(
        String nome,
        String referencia,
        String fornecedor,
        String categoria,
        String marca,
        TipoProduto tipoProduto
) {}