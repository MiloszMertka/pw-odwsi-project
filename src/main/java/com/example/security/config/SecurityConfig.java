package com.example.security.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SecurityConfig {

    private final EnhancedDaoAuthenticationProvider authenticationProvider;
    private final TotpAuthenticationDetailsSource authenticationDetailsSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(Customizer.withDefaults())
                .headers(headers -> headers
                        .xssProtection(xssProtectionConfig -> xssProtectionConfig.headerValue(HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(contentSecurityPolicyConfig -> contentSecurityPolicyConfig
                                .policyDirectives("default-src 'self'; img-src * data:; object-src 'none'; upgrade-insecure-requests;"))
                )
                .requiresChannel(requiresChannel -> requiresChannel
                        .anyRequest().requiresSecure())
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/users/login").permitAll()
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/users/password-reset").permitAll()
                        .requestMatchers("/users/change-password").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .authenticationDetailsSource(authenticationDetailsSource)
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

}
