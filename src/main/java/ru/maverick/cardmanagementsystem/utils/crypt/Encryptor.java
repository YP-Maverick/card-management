package ru.maverick.cardmanagementsystem.utils.crypt;

public interface Encryptor {
    String encrypt(String data);
    String decrypt(String encryptedData);
}