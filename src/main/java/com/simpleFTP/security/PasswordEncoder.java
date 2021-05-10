package com.simpleFTP.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

// Left for future use in password encryption

public class PasswordEncoder {
    private SecureRandom secureRandom;
    private SecretKeyFactory secretKeyFactory;

    public PasswordEncoder () throws NoSuchAlgorithmException {
        this.secureRandom  = new SecureRandom();
        this.secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    }

    public byte[] encrypt(String password) throws InvalidKeySpecException {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);

        return secretKeyFactory.generateSecret(spec).getEncoded();
    }
}
