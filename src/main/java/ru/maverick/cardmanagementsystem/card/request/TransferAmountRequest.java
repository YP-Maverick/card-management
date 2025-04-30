package ru.maverick.cardmanagementsystem.card.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class TransferAmountRequest {
    @NotNull
    String fromCardNumber;

    @NotNull
    String toCardNumber;

    @Positive
    BigDecimal amount;
}
