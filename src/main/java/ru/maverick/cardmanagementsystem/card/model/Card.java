package ru.maverick.cardmanagementsystem.card.model;

import jakarta.persistence.*;
import lombok.*;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.user.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder(builderClassName = "CardBuilder", access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = {"encryptedCardNumber", "lastFourDigits"})
@Entity
@EntityListeners(CardEntityListener.class)
@Table(
        name = "cards",
        indexes = @Index(
                name = "idx_cards_encrypted_number",
                columnList = "encryptedCardNumber",
                unique = true
        )
)
public class Card {

    /**
     * Сущность, представляющая банковскую карту.
     * <p>
     * Содержит зашифрованный номер карты и последние 4 цифры для отображения.
     * Шифрование и генерация полей выполняются через {@link CardEntityListener}.
     * <p>
     * Создать извне можно только через Builder
     */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Transient
    private String rawCardNumber;

    @Column(nullable = false)
    private String encryptedCardNumber;

    @Column(nullable = false)
    private String lastFourDigits;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private Instant createdAt;
    private Instant updatedAt;


    // Установить значения encryptedCardNumber и lastFourDigits нельзя, ими управляет CardEntityListener
    public static class CardBuilder {

        private CardBuilder encryptedCardNumber(String encryptedCardNumber) {
            throw new UnsupportedOperationException("Используйте rawCardNumber вместо этого");
        }

        private CardBuilder lastFourDigits(String lastFourDigits) {
            throw new UnsupportedOperationException("Поле генерируется автоматически");
        }
    }
}

