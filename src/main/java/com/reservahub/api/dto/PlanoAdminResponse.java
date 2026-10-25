package com.reservahub.api.dto;

import java.math.BigDecimal;

public record PlanoAdminResponse(
        String id,
        String tipo,
        BigDecimal preco_mensal
) {
}