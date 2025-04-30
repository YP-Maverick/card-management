package ru.maverick.cardmanagementsystem.card.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;


@Value
@Builder
public class CardCreateRequest {

    // TODO Генерация cardNumber
    @NotBlank
    String cardNumber;

    @Future
    LocalDate expirationDate;

    @NotNull
    Long userId;

    BigDecimal balance;
}
