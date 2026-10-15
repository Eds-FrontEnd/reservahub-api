package com.reservahub.api.dto;

public record ItemCompraRequest(
        String productId,
        Integer quantity,
        Integer planMonths
) {
}