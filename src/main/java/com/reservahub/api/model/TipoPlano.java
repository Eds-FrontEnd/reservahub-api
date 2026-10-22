package com.reservahub.api.model;

public enum TipoPlano {

    MENSAL("plano-mensal"),
    ANUAL("plano-anual");

    private final String valor;

    TipoPlano(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}