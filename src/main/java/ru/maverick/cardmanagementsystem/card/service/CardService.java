package ru.maverick.cardmanagementsystem.card.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.maverick.cardmanagementsystem.card.model.Card;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.card.request.CardCreateRequest;
import ru.maverick.cardmanagementsystem.user.model.User;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    Card createCard(CardCreateRequest request);

    void updateCardStatus(UUID cardId, CardStatus status);

    Card getCardById(UUID cardId);
    Page<Card> getUserCards(User user, CardStatus cardStatus, Pageable pageable);
    Page<Card> getAllCards(CardStatus status, Pageable pageable);

    void deleteCardById(UUID cardId);

    void transferFundsByCardNumber(String fromRawNumber, String toRawNumber, BigDecimal amount, User user);

    void createCardBlockRequest(String rawCardNumber, User user);
    void approveCardBlockRequest(UUID requestId);
}