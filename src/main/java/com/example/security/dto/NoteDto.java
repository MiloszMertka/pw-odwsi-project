package com.example.security.dto;

import java.util.Set;
import java.util.UUID;

public record NoteDto(
        UUID id,
        String title,
        String content,
        Boolean isEncrypted,
        Boolean isPublic,
        String author,
        Set<String> readers
) {

}