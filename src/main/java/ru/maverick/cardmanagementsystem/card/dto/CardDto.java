package ru.maverick.cardmanagementsystem.card.dto;

import lombok.Builder;
import lombok.Value;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class CardDto {
        UUID id;
        String maskedNumber;
        String ownerName;
        LocalDate expirationDate;
        CardStatus status;
        BigDecimal balance;
        Instant createdAt;
}