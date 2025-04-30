package ru.maverick.cardmanagementsystem.user.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Value;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;

@Value
@Builder
public class UserDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(unique = true)
    String email;

    String fistName;
    String lastName;

    Role role;
}