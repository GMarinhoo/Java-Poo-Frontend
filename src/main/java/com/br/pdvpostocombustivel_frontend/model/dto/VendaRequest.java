package com.br.pdvpostocombustivel_frontend.model.dto;

import java.util.List;

public record VendaRequest(
        Long idFrentista,
        String formaPagamento,
        List<VendaItemRequest> itens
) {}