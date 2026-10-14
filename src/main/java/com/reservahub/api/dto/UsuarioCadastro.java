 package com.reservahub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCadastro(

        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String senha,

        String cep,

        String street,

        String neighborhood,

        String city,

        String state

) {
}
