package com.reservahub.api.dto;

public record LoginResposta(
        String token,
        String nome,
        String perfil
) {
}
