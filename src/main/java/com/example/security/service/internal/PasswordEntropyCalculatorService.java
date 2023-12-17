package com.example.security.service.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class PasswordEntropyCalculatorService {

    private final List<CharSet> stdCharsets = List.of(
            new CharSet("lowercase", Pattern.compile("[a-z]"), 26),
            new CharSet("uppercase", Pattern.compile("[A-Z]"), 26),
            new CharSet("numbers", Pattern.compile("[0-9]"), 10),
            new CharSet("symbols", Pattern.compile("[^a-zA-Z0-9]"), 33)
    );

    public int calculatePasswordEntropy(String password) {
        int charsetSize = stdCharsets.stream()
                .mapToInt(charset -> charset.regex.matcher(password).find() ? charset.size : 0)
                .sum();
        return (int) Math.round(password.length() * Math.log(charsetSize) / Math.log(2));
    }

    private record CharSet(
            String name,
            Pattern regex,
            int size) {

    }

}
