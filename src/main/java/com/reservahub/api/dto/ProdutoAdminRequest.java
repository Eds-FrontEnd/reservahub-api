package com.reservahub.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProdutoAdminRequest(
        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome,

        @Min(value = 0, message = "O estoque não pode ser negativo.")
        Integer stock,

        @NotBlank(message = "Informe o modelo.")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres.")
        String modelo_produto,

        @NotBlank(message = "Informe a descrição.")
        String description,

        @NotBlank(message = "Selecione um plano.")
        String id_plano,

        @NotBlank(message = "Envie uma imagem para o produto.")
        @Size(max = 255, message = "O caminho da imagem é muito longo.")
        String image_url
) {
}