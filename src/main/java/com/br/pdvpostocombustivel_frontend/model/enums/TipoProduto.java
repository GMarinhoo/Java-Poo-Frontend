package com.br.pdvpostocombustivel_frontend.model.enums;

public enum TipoProduto {
    GASOLINA_COMUM("Gasolina Comum"),
    GASOLINA_ADITIVADA("Gasolina Aditivada"),
    DIESEL_S10("Diesel S10"),
    ETANOL_HIDRATADO("Etanol Hidratado");

    private final String descricao;

    private TipoProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}