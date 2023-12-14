package com.example.security.seeder;

import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "seeders.enabled", havingValue = "true", matchIfMissing = true)
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserSeeder implements Seeder {

    private final static String FAKE_PASSWORD = "ZAQ!2wsx";
    private final Faker faker;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed(int objectsNumber) {
        final Set<User> users = HashSet.newHashSet(objectsNumber);
        while (users.size() < objectsNumber) {
            final var user = createUser();
            users.add(user);
        }

        userRepository.saveAll(users);
    }

    @Override
    public void clean() {
        userRepository.deleteAll();
    }

    private User createUser() {
        final var username = faker.name().username();
        final var encodedPassword = passwordEncoder.encode(FAKE_PASSWORD);
        return new User(username, encodedPassword);
    }

}
