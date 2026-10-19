package com.reservahub.api.dto;

import java.time.LocalDateTime;

public record CupomPromocionalResponse(
        Long id,
        String nomeUsuario,
        String emailUsuario,
        String cupom,
        Integer percentual,
        String uf,
        LocalDateTime dataCriacao,
        Boolean utilizado,
        Boolean ativo
) {
}