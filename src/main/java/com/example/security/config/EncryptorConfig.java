package com.example.security.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
class EncryptorConfig {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

}
