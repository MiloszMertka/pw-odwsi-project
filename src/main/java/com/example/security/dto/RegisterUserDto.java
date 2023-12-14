package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(
        @NotBlank(message = "Username cannot be blank") @Size(max = 255, message = "Username must not have more than 255 characters") String username,
        @NotBlank(message = "Password cannot be blank") @Size(min = 8, max = 255, message = "Password must have between 8 and 255 characters") String password,
        @NotBlank(message = "Password confirmation cannot be blank") @Size(min = 8, max = 255, message = "Password confirmation must have between 8 and 255 characters") String passwordConfirmation
) {

}