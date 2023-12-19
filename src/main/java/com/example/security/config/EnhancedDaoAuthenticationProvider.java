package com.example.security.config;

import com.example.security.dto.LastSuccessfulLoginDto;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EnhancedDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private static final String REACHED_MAX_UNSUCCESSFUL_LOGIN_ATTEMPTS = "Reached max unsuccessful login attempts";
    private static final long LOGIN_DELAY = 1000L;
    private static final int MAX_UNSUCCESSFUL_LOGIN_ATTEMPTS = 5;
    private static final int MAX_LAST_SUCCESSFUL_LOGINS = 5;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final Cache<String, Integer> unsuccessfulLoginAttemptsCache;
    private final Cache<UserDetails, List<LastSuccessfulLoginDto>> lastSuccessfulLoginsCache;

    @PostConstruct
    private void init() {
        setPasswordEncoder(passwordEncoder);
        setUserDetailsService(userDetailsService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        checkIfReachedMaxUnsuccessfulLoginAttempts();
        delayLogin();

        try {
            return super.authenticate(authentication);
        } catch (AuthenticationException exception) {
            incrementUnsuccessfulLoginAttempts();
            throw exception;
        }
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        clearUnsuccessfulLoginAttempts();
        addLastSuccessfulLogin(user);
        return super.createSuccessAuthentication(principal, authentication, user);
    }

    private void delayLogin() {
        try {
            Thread.sleep(LOGIN_DELAY);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void checkIfReachedMaxUnsuccessfulLoginAttempts() {
        final var unsuccessfulLoginAttemptsCount = getUnsuccessfulLoginAttemptsCount();

        if (unsuccessfulLoginAttemptsCount >= MAX_UNSUCCESSFUL_LOGIN_ATTEMPTS) {
            final var request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            final var ipAddress = request.getRemoteAddr();
            log.warn(REACHED_MAX_UNSUCCESSFUL_LOGIN_ATTEMPTS + " for IP address: {}", ipAddress);
            throw new BadCredentialsException(REACHED_MAX_UNSUCCESSFUL_LOGIN_ATTEMPTS);
        }
    }

    private void clearUnsuccessfulLoginAttempts() {
        final var request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        final var ipAddress = request.getRemoteAddr();
        unsuccessfulLoginAttemptsCache.invalidate(ipAddress);
    }

    private void incrementUnsuccessfulLoginAttempts() {
        final var request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        final var ipAddress = request.getRemoteAddr();
        final var unsuccessfulLoginAttemptsCount = getUnsuccessfulLoginAttemptsCount() + 1;
        unsuccessfulLoginAttemptsCache.put(ipAddress, unsuccessfulLoginAttemptsCount);
    }

    private int getUnsuccessfulLoginAttemptsCount() {
        final var request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        final var ipAddress = request.getRemoteAddr();
        return unsuccessfulLoginAttemptsCache.get(ipAddress, key -> 0);
    }

    private void addLastSuccessfulLogin(UserDetails user) {
        final var request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        final var lastSuccessfulLogins = lastSuccessfulLoginsCache.get(user, key -> new LinkedList<>());
        final var lastSuccessfulLogin = new LastSuccessfulLoginDto(request.getRemoteAddr(), request.getHeader("User-Agent"));
        lastSuccessfulLogins.add(lastSuccessfulLogin);

        while (lastSuccessfulLogins.size() > MAX_LAST_SUCCESSFUL_LOGINS) {
            lastSuccessfulLogins.removeFirst();
        }

        lastSuccessfulLoginsCache.put(user, lastSuccessfulLogins);
    }

}
