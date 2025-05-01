package ru.maverick.cardmanagementsystem.jwt.repository;

import ru.maverick.cardmanagementsystem.jwt.model.Token;
import ru.maverick.cardmanagementsystem.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JWTRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token);

    List<Token> findAllByUser(User user);

    long countByUserAndRevokedFalseAndExpiredFalse(User user);
}
