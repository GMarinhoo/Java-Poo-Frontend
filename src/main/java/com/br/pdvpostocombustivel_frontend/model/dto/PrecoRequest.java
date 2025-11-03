package com.br.pdvpostocombustivel_frontend.model.dto;

import java.math.BigDecimal;

public record PrecoRequest(
        BigDecimal valor,
        Long idProduto
) {}