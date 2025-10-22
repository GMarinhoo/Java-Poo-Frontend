package com.br.pdvpostocombustivel_frontend.model.enums;

public enum TipoEstoque {
    COMBUSTIVEL("Combustível"),
    LUBRIFICANTE("Lubrificante"),
    CONVENIENCIA("Conveniência"),
    OUTROS("Outros");

    private final String descricao;

    private TipoEstoque(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}