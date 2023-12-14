package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserDto(
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String password
) {

}