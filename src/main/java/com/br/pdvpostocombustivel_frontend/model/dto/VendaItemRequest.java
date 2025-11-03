package com.br.pdvpostocombustivel_frontend.model.dto;

import java.math.BigDecimal;

public record VendaItemRequest(
        Long idProduto,
        BigDecimal quantidade
) {}