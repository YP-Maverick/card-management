package ru.maverick.cardmanagementsystem.card.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.maverick.cardmanagementsystem.utils.crypt.Encryptor;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CardEntityListener {

    /**
     * Слушатель событий JPA для управления данными карты при сохранении и обновлении.
     * <p>
     * Управляет:
     * <ul>
     *   <li>Шифрованием номера карты при создании entity.</li>
     *   <li>Генерацией последних 4 цифр номера для отображения.</li>
     *   <li>Установкой временных меток (createdAt, updatedAt).</li>
     * </ul>
     */

    private final Encryptor encryptionService;

    @PrePersist
    public void prePersist(Card card) {

        Instant now = Instant.now();
        card.setCreatedAt(now);
        card.setUpdatedAt(now);

        if (card.getRawCardNumber() == null) return;

        card.setEncryptedCardNumber(encryptionService.encrypt(card.getRawCardNumber()));
        card.setLastFourDigits(card.getRawCardNumber().substring(12));
        card.setRawCardNumber(null);
    }

    @PreUpdate
    public void preUpdate(Card card) {
        card.setUpdatedAt(Instant.now());
    }
}