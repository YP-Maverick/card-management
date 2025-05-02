package ru.maverick.cardmanagementsystem.card.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;


@Value
@Builder
public class CardCreateRequest {

    @Future
    LocalDate expirationDate;

    @NotNull
    Integer userId;

    @PositiveOrZero
    BigDecimal balance;
}
