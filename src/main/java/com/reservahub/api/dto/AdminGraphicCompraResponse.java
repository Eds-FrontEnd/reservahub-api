package com.reservahub.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminGraphicCompraResponse(
        Long idDetalhe,
        Integer quantidade,
        String idPlano,
        String idProduto,
        Integer mesesContratados,
        String tipoPlano,
        BigDecimal valorOriginal,
        String cupomDesconto,
        BigDecimal valorTotal,
        LocalDateTime dataCompra
) {
}