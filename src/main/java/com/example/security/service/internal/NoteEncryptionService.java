package com.example.security.service.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class NoteEncryptionService {

    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int KEY_ITERATIONS = 1000;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();

    public String encrypt(String note, String password) {
        final var salt = generateSalt();
        final var iv = generateIV();
        final var cipher = initCipher(password, salt, iv, Cipher.ENCRYPT_MODE);
        return encryptNote(note, cipher, salt, iv);
    }

    public String decrypt(String note, String password) {
        final var salt = getSaltFromEncryptedString(note);
        final var iv = getIVFromEncryptedString(note);
        final var cipher = initCipher(password, salt, iv, Cipher.DECRYPT_MODE);
        return decryptNote(note, cipher);
    }

    private byte[] generateSalt() {
        final var salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] getSaltFromEncryptedString(String encryptedString) {
        final var encryptedBytes = Base64.getDecoder().decode(encryptedString);
        final var salt = new byte[SALT_LENGTH];
        System.arraycopy(encryptedBytes, IV_LENGTH, salt, 0, SALT_LENGTH);
        return salt;
    }

    private byte[] generateIV() {
        final var iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    private byte[] getIVFromEncryptedString(String encryptedString) {
        final var encryptedBytes = Base64.getDecoder().decode(encryptedString);
        final var iv = new byte[IV_LENGTH];
        System.arraycopy(encryptedBytes, 0, iv, 0, IV_LENGTH);
        return iv;
    }

    private SecretKeySpec createSecretKeySpec(String password, byte[] salt) {
        try {
            final var spec = new PBEKeySpec(password.toCharArray(), salt, KEY_ITERATIONS, KEY_LENGTH);
            final var keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            final var key = keyFactory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Cipher initCipher(String password, byte[] salt, byte[] iv, int mode) {
        try {
            final var keySpec = createSecretKeySpec(password, salt);
            final var ivSpec = new IvParameterSpec(iv);
            final var cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(mode, keySpec, ivSpec);
            return cipher;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private String encryptNote(String note, Cipher cipher, byte[] salt, byte[] iv) {
        try {
            final var encryptedNote = cipher.doFinal(note.getBytes());
            final var encryptedNoteWithSaltAndIV = new byte[encryptedNote.length + IV_LENGTH + SALT_LENGTH];
            System.arraycopy(iv, 0, encryptedNoteWithSaltAndIV, 0, IV_LENGTH);
            System.arraycopy(salt, 0, encryptedNoteWithSaltAndIV, IV_LENGTH, SALT_LENGTH);
            System.arraycopy(encryptedNote, 0, encryptedNoteWithSaltAndIV, IV_LENGTH + SALT_LENGTH, encryptedNote.length);
            return Base64.getEncoder().encodeToString(encryptedNoteWithSaltAndIV);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private String decryptNote(String note, Cipher cipher) {
        try {
            final var encryptedBytes = Base64.getDecoder().decode(note);
            final var encryptedNote = new byte[encryptedBytes.length - IV_LENGTH - SALT_LENGTH];
            System.arraycopy(encryptedBytes, IV_LENGTH + SALT_LENGTH, encryptedNote, 0, encryptedNote.length);
            final var decryptedBytes = cipher.doFinal(encryptedNote);
            return new String(decryptedBytes);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}
