package org.acme.user;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateUserDto(

        Long id,
        @NotBlank(message = "Campo nome é obrigatorio")
        @Length(min = 2, max = 120, message = "O campo nome deve ter entre 2 e 120 caracteres")
        String firstName,
        @NotBlank(message = "Campo sobrenome é obrigatorio")
        @Length(min = 2, max = 120, message = "O campo sobrenome deve ter entre 2 e 120 caracteres")
        String lastName,

        @NotBlank(message = "Campo sexo é obrigatorio")
        String gender
) {
}
