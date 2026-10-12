package com.reservahub.api.dto;

import java.time.LocalDateTime;

public record SolicitacaoResetSenhaResposta(
        Long id,
        String email,
        String status,
        LocalDateTime criadoEm
) {
}