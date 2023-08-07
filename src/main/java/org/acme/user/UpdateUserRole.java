package org.acme.user;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UpdateUserRole(

        Long id,
        @NotBlank(message = "We need one role at least")
        Set<String> roles
) {

}
