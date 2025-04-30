package ru.maverick.cardmanagementsystem.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.maverick.cardmanagementsystem.card.model.CardBlockRequest;

import java.util.UUID;

public interface BlockRequestRepository extends JpaRepository<CardBlockRequest, UUID> {
}
