package ru.maverick.cardmanagementsystem.card.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.maverick.cardmanagementsystem.card.model.Card;
import ru.maverick.cardmanagementsystem.card.model.CardBlockRequest;
import ru.maverick.cardmanagementsystem.card.model.enums.CardBlockRequestStatus;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.card.repository.BlockRequestRepository;
import ru.maverick.cardmanagementsystem.card.repository.CardRepository;
import ru.maverick.cardmanagementsystem.card.request.CardCreateRequest;
import ru.maverick.cardmanagementsystem.exception.InsufficientFundsException;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;
import ru.maverick.cardmanagementsystem.user.service.UserService;
import ru.maverick.cardmanagementsystem.utils.crypt.Encryptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BlockRequestRepository blockRequestRepository;

    @Mock
    private Encryptor encryptor;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private Card testCard;
    private final UUID testCardId = UUID.randomUUID();
    private final String rawCardNumber = "1234567890123456";
    private final String encryptedCardNumber = "encrypted-123456";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .email("user@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        testCard = Card.builder()
                .id(testCardId)
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .rawCardNumber(rawCardNumber)
                .build();
    }


    @Test
    void createCard_ValidRequest_ReturnsCardWithCorrectFields() {
        // Arrange
        CardCreateRequest request = CardCreateRequest.builder()
                .userId(1)
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(userService.getUserById(1)).thenReturn(testUser);
        when(cardNumberGenerator.generateUniqueCardNumber()).thenReturn(rawCardNumber);
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Card result = cardService.createCard(request);

        // Assert
        assertAll(
                () -> assertEquals(testUser, result.getOwner()),
                () -> assertEquals(request.getExpirationDate(), result.getExpirationDate()),
                () -> assertEquals(CardStatus.ACTIVE, result.getStatus())
        );
    }

    @Test
    void updateCardStatus_ValidRequest_UpdatesStatus() {
        // Arrange
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        // Act
        cardService.updateCardStatus(testCardId, CardStatus.BLOCKED);

        // Assert
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
    }

    @Test
    void deleteCard_ExistingCard_DeletesFromRepository() {
        // Arrange
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        // Act
        cardService.deleteCardById(testCardId);

        // Assert
        verify(cardRepository).delete(testCard);
    }

    @Test
    void getCardById_ExistingCard_ReturnsCard() {
        // Arrange
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        // Act
        Card result = cardService.getCardById(testCardId);

        // Assert
        assertEquals(testCard, result);
    }

    @Test
    void getUserCards_WithFilter_ReturnsFilteredCards() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(cardRepository.findByOwnerAndStatus(testUser, CardStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(testCard)));

        // Act
        Page<Card> result = cardService.getUserCards(testUser, CardStatus.ACTIVE, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testCard, result.getContent().get(0));
    }

    @Nested
    class BlockRequestTests {
        private CardBlockRequest testRequest;

        @BeforeEach
        void setup() {
            testRequest = CardBlockRequest.builder()
                    .id(UUID.randomUUID())
                    .encryptedCardNumber(encryptedCardNumber)
                    .requester(testUser)
                    .status(CardBlockRequestStatus.PENDING)
                    .build();
        }

        @Test
        void createBlockRequest_ValidRequest_SavesRequest() {
            // Arrange
            when(encryptor.encrypt(rawCardNumber)).thenReturn(encryptedCardNumber);
            when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber))
                    .thenReturn(Optional.of(testCard));
            when(blockRequestRepository.save(any())).thenReturn(testRequest);

            // Act
            cardService.createCardBlockRequest(rawCardNumber, testUser);

            // Assert
            verify(blockRequestRepository).save(argThat(request ->
                    request.getEncryptedCardNumber().equals(encryptedCardNumber) &&
                            request.getRequester().equals(testUser)
            ));
        }

        @Test
        void approveBlockRequest_ValidRequest_UpdatesStatus() {
            // Arrange
            when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber))
                    .thenReturn(Optional.of(testCard));
            when(blockRequestRepository.findById(testRequest.getId()))
                    .thenReturn(Optional.of(testRequest));

            // Act
            cardService.approveCardBlockRequest(testRequest.getId());

            // Assert
            assertAll(
                    () -> assertEquals(CardBlockRequestStatus.APPROVED, testRequest.getStatus()),
                    () -> assertEquals(CardStatus.BLOCKED, testCard.getStatus()),
                    () -> assertNotNull(testRequest.getResolvedAt())
            );
        }
    }

    @Nested
    class TransferTests {
        private Card fromCard;
        private Card toCard;
        private final BigDecimal amount = BigDecimal.valueOf(100);

        @BeforeEach
        void setup() {
            // Arrange - общие данные для тестов перевода
            fromCard = Card.builder()
                    .balance(BigDecimal.valueOf(500))
                    .owner(testUser)
                    .status(CardStatus.ACTIVE)
                    .expirationDate(LocalDate.now().plusYears(2))
                    .build();

            toCard = Card.builder()
                    .id(UUID.randomUUID())
                    .owner(testUser)
                    .balance(BigDecimal.ZERO)
                    .status(CardStatus.ACTIVE)
                    .expirationDate(LocalDate.now().plusYears(2))
                    .build();

            when(encryptor.encrypt(rawCardNumber)).thenReturn(encryptedCardNumber);
            when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber))
                    .thenReturn(Optional.of(fromCard))
                    .thenReturn(Optional.of(toCard));
        }

        @Test
        void transferFunds_ValidRequest_UpdatesBalances() {
            // Act
            cardService.transferFundsByCardNumber(rawCardNumber, rawCardNumber, amount, testUser);

            // Assert
            assertAll(
                    () -> assertEquals(BigDecimal.valueOf(400), fromCard.getBalance()),
                    () -> assertEquals(amount, toCard.getBalance()),
                    () -> verify(cardRepository).saveAll(List.of(fromCard, toCard))
            );
        }

        @Test
        void transferFunds_InsufficientBalance_ThrowsException() {
            // Arrange
            BigDecimal bigAmount = BigDecimal.valueOf(1000);

            // Act and Assert
            assertThrows(InsufficientFundsException.class, () ->
                    cardService.transferFundsByCardNumber(rawCardNumber, rawCardNumber, bigAmount, testUser));
        }
    }

}