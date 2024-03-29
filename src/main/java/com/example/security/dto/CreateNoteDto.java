package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNoteDto(
        @NotBlank(message = "Title cannot be blank") @Size(max = 255, message = "Title must not have more than 255 characters") String title,
        @NotBlank(message = "Content cannot be blank") @Size(max = 5000, message = "Content must not have more than 5000 characters") String content,
        @Size(max = 255, message = "Password must not have more than 255 characters") String password,
        @NotNull(message = "Is public cannot be null") Boolean isPublic
) {

}