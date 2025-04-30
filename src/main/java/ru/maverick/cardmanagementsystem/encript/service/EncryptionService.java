package ru.maverick.cardmanagementsystem.encript.service;

public interface EncryptionService {
    String encrypt(String data);
    String decrypt(String encryptedData);
}