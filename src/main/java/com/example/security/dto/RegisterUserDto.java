package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(min = 8, max = 255) String password,
        @NotBlank @Size(min = 8, max = 255) String passwordConfirmation
) {

}