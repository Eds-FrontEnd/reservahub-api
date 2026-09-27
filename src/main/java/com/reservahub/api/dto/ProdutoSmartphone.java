package com.reservahub.api.dto;

public record ProdutoSmartphone(
        String id,
        String nome,
        Integer stock,
        String modelo_produto,
        String image_url,
        String id_plano
) {
}