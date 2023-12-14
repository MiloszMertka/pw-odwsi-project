package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteDto(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 5000) String content
) {

}