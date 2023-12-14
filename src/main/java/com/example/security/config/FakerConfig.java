package com.example.security.config;

import com.github.javafaker.Faker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@ConditionalOnProperty(name = "seeders.enabled", havingValue = "true", matchIfMissing = true)
class FakerConfig {

    private static final long SEED = 1234L;

    @Bean
    public Faker faker() {
        final var random = new Random(SEED);
        return new Faker(random);
    }

}
