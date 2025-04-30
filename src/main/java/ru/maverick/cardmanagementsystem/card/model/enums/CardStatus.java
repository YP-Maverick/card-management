package ru.maverick.cardmanagementsystem.card.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус карты", example = "ACTIVE")
public enum CardStatus {
    @Schema(description = "Активна") ACTIVE,
    @Schema(description = "Заблокирована") BLOCKED,
    @Schema(description = "Истек срок действия") EXPIRED,
    @Schema(description = "Ожидает блокировки") PENDING_BLOCK
}