package com.example.security.service.internal;

import com.example.security.dto.*;
import com.example.security.model.PasswordResetToken;
import com.example.security.model.User;
import com.example.security.repository.PasswordResetTokenRepository;
import com.example.security.repository.UserRepository;
import com.example.security.service.UserUseCases;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserService implements UserUseCases, UserDetailsService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String USER_ALREADY_EXISTS_MESSAGE = "User already exists";
    private static final String PASSWORDS_DO_NOT_MATCH_MESSAGE = "Passwords do not match";
    private static final String PASSWORD_TOO_WEAK_MESSAGE = "Password is too weak";
    private static final String USER_EMAIL_NOT_FOUND_MESSAGE = "User with given email not found";
    private static final String INVALID_TOKEN_MESSAGE = "Invalid token";
    private static final int MINIMUM_PASSWORD_ENTROPY = 70;
    public static String QR_PREFIX =
            "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEntropyCalculatorService passwordEntropyCalculatorService;
    private final PasswordResetEmailService passwordResetEmailService;
    private final Cache<UserDetails, List<LastSuccessfulLoginDto>> lastSuccessfulLoginsCache;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS_MESSAGE));
    }

    @Override
    @Transactional
    public String registerUser(@NonNull RegisterUserDto registerUserDto) {
        validateUserDoesNotExist(registerUserDto.username(), registerUserDto.email());
        validatePassword(registerUserDto.password(), registerUserDto.passwordConfirmation());
        final var encodedPassword = passwordEncoder.encode(registerUserDto.password());
        final var user = new User(registerUserDto.username(), registerUserDto.email(), encodedPassword);
        final var qrUrl = generateQRUrl(user);
        userRepository.save(user);
        return qrUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllOtherUsers(@NonNull User user) {
        return userRepository.findAll().stream()
                .filter(otherUser -> !otherUser.equals(user))
                .map(this::mapUserToUserDto)
                .toList();
    }

    @Override
    public List<LastSuccessfulLoginDto> getLastSuccessfulLogins(@NonNull User user) {
        return lastSuccessfulLoginsCache.get(user, key -> List.of()).reversed();
    }

    @Override
    @Transactional
    public void createPasswordResetToken(@NonNull CreatePasswordResetTokenDto createPasswordResetTokenDto) {
        final var user = userRepository.findByEmail(createPasswordResetTokenDto.email())
                .orElseThrow(() -> new IllegalStateException(USER_EMAIL_NOT_FOUND_MESSAGE));
        final var token = new PasswordResetToken(user);
        passwordResetEmailService.sendPasswordResetEmail(user, token);
        passwordResetTokenRepository.save(token);
    }

    @Override
    @Transactional
    public boolean isPasswordResetTokenValid(String token) {
        if (token == null) {
            return false;
        }

        final var passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken.isEmpty()) {
            return false;
        }

        if (passwordResetToken.get().isExpired()) {
            passwordResetTokenRepository.delete(passwordResetToken.get());
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public void changePassword(@NonNull ChangePasswordDto changePasswordDto) {
        final var passwordResetToken = passwordResetTokenRepository.findByToken(changePasswordDto.token())
                .orElseThrow(() -> new IllegalStateException(INVALID_TOKEN_MESSAGE));
        validatePassword(changePasswordDto.password(), changePasswordDto.passwordConfirmation());
        final var encodedPassword = passwordEncoder.encode(changePasswordDto.password());
        final var user = passwordResetToken.getUser();
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private String generateQRUrl(User user) {
        return QR_PREFIX + URLEncoder.encode(String.format(
                        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                        "security", user.getEmail(), user.getTotpSecret(), "security"),
                StandardCharsets.UTF_8);
    }

    private void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
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
