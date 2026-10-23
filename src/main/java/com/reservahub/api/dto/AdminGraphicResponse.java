package com.reservahub.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminGraphicResponse(
        Integer ano,
        Long totalRegistros,
        Long totalUnidades,
        Long planosMensais,
        Long planosAnuais,
        BigDecimal faturamentoTotal,
        List<AdminGraphicMesResponse> faturamentoPorMes,
        List<AdminGraphicCompraResponse> compras
) {
}