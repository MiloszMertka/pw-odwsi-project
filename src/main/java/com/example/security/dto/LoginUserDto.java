package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserDto(
        @NotBlank(message = "Username cannot be blank") @Size(max = 255, message = "Username must not have more than 255 characters") String username,
        @NotBlank(message = "Password cannot be blank") @Size(max = 255, message = "Password must not have more than 255 characters") String password
) {

}