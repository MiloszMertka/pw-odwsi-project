package com.example.security.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SecurityConfig {

    private static final long LOGIN_DELAY = 1000L;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(Customizer.withDefaults())
                .headers(headers -> headers
                        .xssProtection(xssProtectionConfig -> xssProtectionConfig.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(contentSecurityPolicyConfig -> contentSecurityPolicyConfig
                                .policyDirectives("default-src 'self'; img-src *; object-src 'none'; upgrade-insecure-requests; require-trusted-types-for 'script';"))
                )
                .requiresChannel(requiresChannel -> requiresChannel
                        .anyRequest().requiresSecure())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/users/login").permitAll()
                        .requestMatchers("/users/register").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .defaultSuccessUrl("/notes", true)
                        .failureUrl("/users/login?error=true"))
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        final var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPreAuthenticationChecks(toCheck -> {
            try {
                Thread.sleep(LOGIN_DELAY);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        });
        return authenticationProvider;
    }

}
