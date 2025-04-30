package ru.maverick.cardmanagementsystem.encript.service;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionServiceImpl implements EncryptionService {
    private final AES256TextEncryptor encryptor;

    public EncryptionServiceImpl(@Value("${encryption.secret}") String secret) {
        encryptor = new AES256TextEncryptor();
        encryptor.setPassword(secret);
    }

    @Override
    public String encrypt(String data) {
        return encryptor.encrypt(data);
    }

    @Override
    public String decrypt(String encryptedData) {
        return encryptor.decrypt(encryptedData);
    }
}
