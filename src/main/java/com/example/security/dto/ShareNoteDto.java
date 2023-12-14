package com.example.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ShareNoteDto(
        @NotNull(message = "Usernames cannot be null") Set<@NotBlank(message = "Username cannot be blank") @Size(max = 255, message = "Username must not have more than 255 characters") String> usernames
) {

}
