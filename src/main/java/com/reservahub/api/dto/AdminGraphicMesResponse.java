package com.reservahub.api.dto;

import java.math.BigDecimal;

public record AdminGraphicMesResponse(
        Integer mes,
        Long quantidadeMensal,
        Long quantidadeAnual,
        BigDecimal faturamentoMensal,
        BigDecimal faturamentoAnual,
        BigDecimal faturamentoTotal
) {
}