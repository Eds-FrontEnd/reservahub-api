
package com.reservahub.api.dto;

import java.time.LocalDateTime;

public record SolicitacaoResetSenhaStatusResposta(
        Long id,
        String status,
        LocalDateTime criadoEm
) {
}
