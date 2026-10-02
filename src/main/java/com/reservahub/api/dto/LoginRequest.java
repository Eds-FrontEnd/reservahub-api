package com.reservahub.api.dto;

public record LoginRequest(
        String email,
        String senha
) {
}