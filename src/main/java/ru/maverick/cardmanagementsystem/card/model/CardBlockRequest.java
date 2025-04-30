package ru.maverick.cardmanagementsystem.card.model;

import jakarta.persistence.*;
import lombok.*;
import ru.maverick.cardmanagementsystem.card.model.enums.CardBlockRequestStatus;
import ru.maverick.cardmanagementsystem.user.model.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "block_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBlockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String encryptedCardNumber;

    @Enumerated(EnumType.STRING)
    private CardBlockRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User requester;

    private Instant createdAt;
    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

