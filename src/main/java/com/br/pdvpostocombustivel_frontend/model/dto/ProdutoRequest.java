package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoProduto;

// Campos devem bater com o backend!
public record ProdutoRequest(
        String nome,
        String referencia,
        String fornecedor,
        String categoria,
        String marca,
        TipoProduto tipo
) {}