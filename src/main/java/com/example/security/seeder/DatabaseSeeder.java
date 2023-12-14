package com.example.security.seeder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "seeders.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DatabaseSeeder implements CommandLineRunner {

    private final UserSeeder userSeeder;
    private final NoteSeeder noteSeeder;

    @Override
    public void run(String... args) throws Exception {
        cleanDatabase();
        seedDatabase();
    }

    private void cleanDatabase() {
        noteSeeder.clean();
        userSeeder.clean();
    }

    private void seedDatabase() {
        userSeeder.seed(5);
        noteSeeder.seed(20);
    }

}
