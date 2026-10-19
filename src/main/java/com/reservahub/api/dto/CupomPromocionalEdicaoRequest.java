package com.reservahub.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CupomPromocionalEdicaoRequest(
        @NotBlank(message = "Informe o código do cupom.")
        @Pattern(
                regexp = "[A-Za-z0-9_-]{3,20}",
                message = "O código deve conter de 3 a 20 caracteres: letras, números, hífen ou sublinhado."
        )
        String cupom,

        @NotNull(message = "Informe o percentual de desconto.")
        @Min(value = 1, message = "O percentual mínimo é 1.")
        @Max(value = 100, message = "O percentual máximo é 100.")
        Integer percentual,

        @NotNull(message = "Informe o status de utilização do cupom.")
        Boolean utilizado
) {
}