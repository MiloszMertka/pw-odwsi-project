package com.example.security.service.internal;

import com.example.security.dto.RegisterUserDto;
import com.example.security.dto.UserDto;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import com.example.security.service.UserUseCases;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserService implements UserUseCases, UserDetailsService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String USER_ALREADY_EXISTS_MESSAGE = "User already exists";
    private static final String PASSWORDS_DO_NOT_MATCH_MESSAGE = "Passwords do not match";
    private static final String PASSWORD_TOO_WEAK_MESSAGE = "Password is too weak";
    private static final int MINIMUM_PASSWORD_ENTROPY = 70;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEntropyCalculatorService passwordEntropyCalculatorService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));
    }

    @Override
    @Transactional
    public void registerUser(@NonNull RegisterUserDto registerUserDto) {
        validateUserDoesNotExist(registerUserDto.username());
        validatePassword(registerUserDto.password(), registerUserDto.passwordConfirmation());
        final var encodedPassword = passwordEncoder.encode(registerUserDto.password());
        final var user = new User(registerUserDto.username(), encodedPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllOtherUsers(@NonNull User user) {
        return userRepository.findAll().stream()
                .filter(otherUser -> !otherUser.equals(user))
                .map(this::mapUserToUserDto)
                .toList();
    }

    private void validateUserDoesNotExist(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException(USER_ALREADY_EXISTS_MESSAGE);
        }
    }

    private void validatePassword(String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            throw new IllegalStateException(PASSWORDS_DO_NOT_MATCH_MESSAGE);
        }

        if (passwordEntropyCalculatorService.calculatePasswordEntropy(password) < MINIMUM_PASSWORD_ENTROPY) {
            throw new IllegalStateException(PASSWORD_TOO_WEAK_MESSAGE);
        }
    }

    private UserDto mapUserToUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }

}
