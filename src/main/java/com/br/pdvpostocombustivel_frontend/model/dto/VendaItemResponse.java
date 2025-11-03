package com.br.pdvpostocombustivel_frontend.model.dto;

import java.math.BigDecimal;

public record VendaItemResponse(
        Long id,
        Long idProduto,
        String nomeProduto,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal precoTotal
) {}