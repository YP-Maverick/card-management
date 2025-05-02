package ru.maverick.cardmanagementsystem.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    void deleteById(Integer id);
}