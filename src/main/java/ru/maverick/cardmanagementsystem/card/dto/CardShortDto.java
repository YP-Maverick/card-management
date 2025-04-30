package ru.maverick.cardmanagementsystem.card.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CardShortDto {
    String cardNumber;
    LocalDate expirationDate;
    BigDecimal balance;
}
