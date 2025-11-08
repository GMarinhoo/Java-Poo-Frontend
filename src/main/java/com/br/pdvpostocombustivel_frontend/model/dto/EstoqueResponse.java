package com.br.pdvpostocombustivel_frontend.model.dto;

import com.br.pdvpostocombustivel_frontend.model.enums.TipoEstoque;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EstoqueResponse(
        Long id,
        Long idProduto,
        BigDecimal quantidade,
        String localTanque,
        String loteEndereco,
        String loteFabricacao,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate dataValidade,
        TipoEstoque tipo
) {}
