package ru.maverick.cardmanagementsystem.exception.handler;


import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.maverick.cardmanagementsystem.auth.controller.AuthController;
import ru.maverick.cardmanagementsystem.card.controller.CardController;
import ru.maverick.cardmanagementsystem.exception.*;
import ru.maverick.cardmanagementsystem.user.controller.UserController;
import ru.maverick.cardmanagementsystem.utils.Formatter;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        AuthController.class,
        CardController.class,
        UserController.class
})
public class ExceptionsHandler {

    private void log(Throwable e) {
        log.error("Исключение {}: {}", e, e.getMessage());
    }

    private Map<String, String> createMap(String status, String reason, String message) {
        return Map.of("status", status,
                      "reason", reason,
                      "message", message,
                      "timestamp", LocalDateTime.now().format(
                              Formatter.getFormatter()));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            ValidationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            CardExpiredException.class,
            InsufficientFundsException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValid(final Exception e) {
        log(e);
        return createMap("BAD_REQUEST", "Incorrectly made request", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        log(e);
        return createMap("NOT_FOUND", "The required object was not found", e.getMessage());
    }

    @ExceptionHandler({
            AuthenticationException.class,
            InvalidTokenException.class,
            InvalidTokenFormatException.class,
            TokenExpiredException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthExceptions(final RuntimeException e) {
        log(e);
        return createMap("UNAUTHORIZED", "Authorization error", e.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicate(final DuplicateException e) {
        log(e);
        return createMap("CONFLICT", "Data conflict", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOtherExc(final Exception e) {
        log(e);
        return createMap("INTERNAL_SERVER_ERROR", "Unexpected error", e.getMessage());
    }
}
