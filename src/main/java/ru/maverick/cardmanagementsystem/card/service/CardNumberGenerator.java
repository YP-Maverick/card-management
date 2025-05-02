package ru.maverick.cardmanagementsystem.card.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.maverick.cardmanagementsystem.card.repository.CardRepository;
import ru.maverick.cardmanagementsystem.utils.crypt.Encryptor;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class CardNumberGenerator {

    private final String bankBin;
    private final CardRepository cardRepository;
    private final Encryptor encryptor;

    private static final int ACCOUNT_NUMBER_LENGTH = 9;
    private static final int MAX_GENERATION_ATTEMPTS = 100;
    private static final int CARD_NUMBER_LENGTH = 16;

    public CardNumberGenerator(
            @Value("${bank.bin}") String bankBin,
            CardRepository cardRepository,
            Encryptor encryptor
    ) {
        if (bankBin.length() != 6 || !bankBin.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid BIN format. Must be 6 digits");
        }
        this.bankBin = bankBin;
        this.cardRepository = cardRepository;
        this.encryptor = encryptor;
    }

    /**
     * Проверяет на уникальность номера среди других в бд
     */
    private boolean isNumberUnique(String rawNumber) {
        String encrypted = encryptor.encrypt(rawNumber);
        return !cardRepository.existsByEncryptedCardNumber(encrypted);
    }

    /**
     * Генерирует уникальный валидный номер карты
     * <p>
     * Формат: [BIN(6)] + [Индивидуальный номер(9)] + [Контрольная цифра(1)]
     */
    public String generateUniqueCardNumber() {
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            String number = generateCardNumber();

            // Алгоритм Луна не гарантирует полную уникальность номера потому что имеет минимальный риск коллизии
            if (isNumberUnique(number)) {
                return number;
            }
        }
        throw new IllegalStateException("Failed to generate unique card number after "
                + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String generateCardNumber() {
        // 1. Генерация базовой части номера (15 цифр)
        String base = bankBin + generateAccountNumber() + "0"; // 0 - временная заглушка

        // 2. Расчет контрольной цифры по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(base);

        return base.substring(0, 15) + checkDigit;
    }

    /**
     * Генерирует 9-значный номер счета
     * Формат: случайное число с ведущими нулями
     * <p>
     * Пример: 427190 -> 000427190
     * <p>
     * В реальных кейсах может зависеть от региона, места выдачи
     */
    private String generateAccountNumber() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(10, ACCOUNT_NUMBER_LENGTH));
        return String.format("%0" + ACCOUNT_NUMBER_LENGTH + "d", randomNum);
    }

    /**
     * Алгоритм Луна для расчета контрольной цифры:
     * <p>
     * 1. Начиная с предпоследней цифры (справа налево),
     *    умножаем каждую вторую цифру на 2
     * <p>
     * 2. Суммируем все цифры полученных чисел
     * <p>
     * 3. Добавляем цифры, которые не умножались
     * <p>
     * 4. Контрольная цифра = (10 - (сумма % 10)) % 10
     */
    public int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit / 10) + (digit % 10);
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * Валидация номера карты по алгоритму Луна
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != CARD_NUMBER_LENGTH) {
            return false;
        }
        String base = cardNumber.substring(0, 15);
        int expectedCheckDigit = calculateLuhnCheckDigit(base);
        int actualCheckDigit = Character.getNumericValue(cardNumber.charAt(15));
        return expectedCheckDigit == actualCheckDigit;
    }
}