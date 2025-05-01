package ru.maverick.cardmanagementsystem.exception;

public class InvalidTokenFormatException extends RuntimeException {

    public InvalidTokenFormatException(String message) {
        super(message);
    }
}
