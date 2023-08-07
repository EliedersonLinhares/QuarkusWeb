package org.acme.user;

import jakarta.validation.constraints.NotBlank;

public record UpdatePassword(
        Long id,
        @NotBlank(message = "Campo senha é obrigatorio")
        String password
) {
}
