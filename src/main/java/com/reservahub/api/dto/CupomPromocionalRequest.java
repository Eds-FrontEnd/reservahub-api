package com.reservahub.api.dto;

public record CupomPromocionalRequest(
        String emailUsuario,
        String cupom,
        Integer percentual,
        Boolean ativo,
        String uf
) {
}