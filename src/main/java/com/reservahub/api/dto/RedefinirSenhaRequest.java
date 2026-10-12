
package com.reservahub.api.dto;

public record RedefinirSenhaRequest(
        Long solicitacaoId,
        String novaSenha
) {
}
