package org.acme.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(


        @Email(message = "email inválido")
        String email,


        @NotBlank(message = "Campo senha é obrigatorio")
        String password
) {
}
