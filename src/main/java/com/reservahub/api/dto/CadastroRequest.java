package com.reservahub.api.dto;

public record CadastroRequest(
        String nome,
        String email,
        String senha
) {
}