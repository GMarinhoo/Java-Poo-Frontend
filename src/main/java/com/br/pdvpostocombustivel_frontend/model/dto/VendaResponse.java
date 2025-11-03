package com.br.pdvpostocombustivel_frontend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(
        Long idVenda,
        LocalDateTime dataHora,
        BigDecimal valorTotal,
        String formaPagamento,
        Long idFrentista,
        String nomeFrentista,
        List<VendaItemResponse> itens
) {}