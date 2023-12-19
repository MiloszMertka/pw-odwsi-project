package com.example.security.dto;

public record LastSuccessfulLoginDto(
        String ipAddress,
        String userAgent
) {

}
