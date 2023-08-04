package org.acme.user;


public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String gender
) {
}
