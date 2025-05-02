package ru.maverick.cardmanagementsystem.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.maverick.cardmanagementsystem.card.model.CardBlockRequest;

import java.util.UUID;

@Repository
public interface BlockRequestRepository extends JpaRepository<CardBlockRequest, UUID> {
}
