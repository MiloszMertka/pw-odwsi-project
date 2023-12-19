package com.example.security.config;

import com.example.security.dto.LastSuccessfulLoginDto;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
class CacheConfig {

    @Bean
    public Cache<String, Integer> unsuccessfulLoginAttemptsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public Cache<UserDetails, List<LastSuccessfulLoginDto>> lastSuccessfulLoginsCache() {
        return Caffeine.newBuilder()
                .build();
    }

}
