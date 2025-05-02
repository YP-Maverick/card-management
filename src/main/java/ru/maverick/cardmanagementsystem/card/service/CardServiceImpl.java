package ru.maverick.cardmanagementsystem.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maverick.cardmanagementsystem.card.model.Card;
import ru.maverick.cardmanagementsystem.card.model.CardBlockRequest;
import ru.maverick.cardmanagementsystem.card.model.enums.CardBlockRequestStatus;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.card.repository.BlockRequestRepository;
import ru.maverick.cardmanagementsystem.card.repository.CardRepository;
import ru.maverick.cardmanagementsystem.card.request.CardCreateRequest;
import ru.maverick.cardmanagementsystem.exception.AuthenticationException;
import ru.maverick.cardmanagementsystem.exception.CardExpiredException;
import ru.maverick.cardmanagementsystem.exception.NotFoundException;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.service.UserService;
import ru.maverick.cardmanagementsystem.utils.crypt.Encryptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final UserService userService;

    private final CardRepository cardRepository;
    private final BlockRequestRepository blockRequestRepository;

    private final Encryptor encryptor;
    private final CardNumberGenerator cardNumberGenerator;


    private void validateCard(Card card, User user) {
        validateCardOwnership(card, user);
        validateCardExpiration(card);
        validateCardActive(card);
    }

    private void validateCardOwnership(Card card, User user) {
        if (!card.getOwner().equals(user)) {
            throw new AuthenticationException(
                    String.format("User %s does not own card with ID: %s",
                            user.getId(), card.getId()));
        }
    }

    private void validateCardExpiration(Card card) {
        if (LocalDate.now().isAfter(card.getExpirationDate())) {
            throw new CardExpiredException(
                    String.format("Card with ID: %s has expired", card.getId()));
        }
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException(
                    String.format("Card with ID: %s is not active", card.getId()));
        }
    }

    @Override
    @Transactional
    public Card createCard(CardCreateRequest request) {

        User user = userService.getUserById(request.getUserId());
        String rawCardNumber = cardNumberGenerator.generateUniqueCardNumber();

        log.info("Created card. rawCardNumber={}, userId={}", rawCardNumber, user.getId());
        return cardRepository.save(Card.builder()
                .rawCardNumber(rawCardNumber)
                .owner(user)
                .expirationDate(request.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(request.getBalance())
                .build());
    }

    @Override
    @Transactional
    public void updateCardStatus(UUID cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Card not found with ID: %s", cardId)));
        card.setStatus(status);
        log.info("Successfully updated card status. cardId={}, newStatus={}", cardId, status);
    }

    @Override
    public Card getCardById(UUID cardId) {
        log.info("Retrieved card. cardId={}", cardId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Card not found with ID: %s", cardId)));
    }

    @Override
    public Page<Card> getUserCards(User user, CardStatus cardStatus, Pageable pageable) {
        log.info("Retrieving user cards. userId={}, status={}", user.getId(), cardStatus);
        return cardRepository.findByOwnerAndStatus(user, cardStatus, pageable);
    }

    @Override
    public Page<Card> getAllCards(CardStatus status, Pageable pageable) {
        log.info("Retrieving all cards. status={}", status);
        return status != null
                ? cardRepository.findAllByStatus(status, pageable)
                : cardRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deleteCardById(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Card not found with ID: %s", cardId)));
        cardRepository.delete(card);
        log.info("Successfully deleted card. cardId={}", cardId);
    }

    @Override
    @Transactional
    public void transferFundsByCardNumber(String fromRawNumber, String toRawNumber,
                                          BigDecimal amount, User requester) {
        Card from = cardRepository.findByEncryptedCardNumber(
                        encryptor.encrypt(fromRawNumber))
                .orElseThrow(() -> new NotFoundException("Source card not found"));

        Card to = cardRepository.findByEncryptedCardNumber(
                        encryptor.encrypt(toRawNumber))
                .orElseThrow(() -> new NotFoundException("Destination card not found"));


        validateCard(from, requester);
        validateCard(to, requester);

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.saveAll(List.of(from, to));
        log.info("Successfully transferred funds. fromCardId={}, toCardId={}, amount={}",
                from.getId(), to.getId(), amount);
    }

    @Override
    @Transactional
    public void createCardBlockRequest(String rawCardNumber, User user) {
        String encryptedNumber = encryptor.encrypt(rawCardNumber);
        Card card = cardRepository.findByEncryptedCardNumber(encryptedNumber)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        validateCardOwnership(card, user);

        CardBlockRequest request = blockRequestRepository.save(CardBlockRequest.builder()
                .encryptedCardNumber(encryptedNumber)
                .requester(user)
                .status(CardBlockRequestStatus.PENDING)
                .build());
        log.info("Created block request. requestId={}, userId={}", request.getId(), user.getId());
    }

    @Override
    @Transactional
    public void approveCardBlockRequest(UUID requestId) {
        CardBlockRequest request = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Block request not found with ID: " + requestId));

        Card card = cardRepository.findByEncryptedCardNumber(
                        request.getEncryptedCardNumber())
                .orElseThrow(() -> new NotFoundException("Card not found"));

        card.setStatus(CardStatus.BLOCKED);
        request.setStatus(CardBlockRequestStatus.APPROVED);
        request.setResolvedAt(Instant.now());
        log.info("Approved block request. requestId={}, cardId={}", requestId, card.getId());
    }
}