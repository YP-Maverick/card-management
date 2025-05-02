package ru.maverick.cardmanagementsystem.card.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.maverick.cardmanagementsystem.card.model.Card;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.user.model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT c FROM " +
           "Card c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "c.owner = :user")
    Page<Card> findByOwnerAndStatus(
            @Param("user") User user,
            @Param("status") CardStatus status,
            Pageable pageable
    );

    @Query("SELECT c FROM " +
           "Card c WHERE " +
           "(:status IS NULL OR c.status = :status)")
    Page<Card> findAllByStatus(
            @Param("status") CardStatus status,
            Pageable pageable
    );

    @Query("SELECT c " +
           "FROM Card c " +
           "WHERE c.encryptedCardNumber = :encryptedNumber")
    Optional<Card> findByEncryptedCardNumber(@Param("encryptedNumber") String encryptedNumber);

    boolean existsByEncryptedCardNumber(String encryptedNumber);
}