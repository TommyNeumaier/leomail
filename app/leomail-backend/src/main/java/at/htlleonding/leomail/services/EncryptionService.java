package at.htlleonding.leomail.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BIT = 128; // 

    @ConfigProperty(name = "quarkus.application.encryption.key")
    public String encryptionKeyBase64;

    /**
     * Encrypts the given value using AES-GCM with a random IV.
     *
     * @param value The value to encrypt.
     * @return The encrypted value, with the IV prepended.
     */
    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec secretKey = getSecretKey(); // Validate and retrieve the AES key

            byte[] iv = generateIV(); // Generate a random IV
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIV = new byte[IV_SIZE + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIV, 0, IV_SIZE);
            System.arraycopy(encrypted, 0, encryptedWithIV, IV_SIZE, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIV); // Encode to Base64
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting", e);
        }
    }

    /**
     * Decrypts the given encrypted value (with IV prepended).
     *
     * @param encryptedValue The encrypted value to decrypt.
     * @return The decrypted value.
     */
    public String decrypt(String encryptedValue) {
        try {
            byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);

            // Extract the IV from the decoded value
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(decodedValue, 0, iv, 0, IV_SIZE);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            SecretKeySpec secretKey = getSecretKey(); // Retrieve the AES key

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decrypted = cipher.doFinal(decodedValue, IV_SIZE, decodedValue.length - IV_SIZE);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting", e);
        }
    }

    /**
     * Retrieves the AES key from the Base64-encoded string.
     *
     * @return A SecretKeySpec object containing the AES key.
     */
    private SecretKeySpec getSecretKey() {
        // Decode the Base64 encoded key
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);

        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length + " bytes. Must be 16, 24, or 32 bytes.");
        }

        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Generates a random IV for AES-GCM encryption.
     *
     * @return A byte array containing the IV.
     */
    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}