package com.example.security.dto;

import jakarta.validation.constraints.Size;

public record GetNoteDto(
        @Size(max = 255, message = "Password must not have more than 255 characters") String password
) {

}