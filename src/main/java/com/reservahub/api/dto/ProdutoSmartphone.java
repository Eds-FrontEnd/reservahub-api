package com.reservahub.api.dto;

import java.math.BigDecimal;

public record ProdutoSmartphone(
        String id,
        String nome,
        Integer stock,
        String modelo_produto,
        String description,
        String image_url,
        String id_plano,
        String plano,
        BigDecimal preco_mensal
) {
}