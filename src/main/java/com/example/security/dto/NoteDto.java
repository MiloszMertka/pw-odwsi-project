package com.example.security.dto;

import java.util.UUID;

public record NoteDto(
        UUID id,
        String title,
        String content,
        Boolean isPublic,
        String author
) {

}