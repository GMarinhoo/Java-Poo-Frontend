package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoEstoque;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EstoqueRequest(
        BigDecimal quantidade,
        String localTanque,
        String loteEndereco,
        String loteFabricacao,
        LocalDate dataValidade,
        TipoEstoque tipo
) {}