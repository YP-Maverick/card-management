package ru.maverick.cardmanagementsystem.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;
import ru.maverick.cardmanagementsystem.user.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class FirstAdminConfigurator implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userService.existsByEmail("admin@system.local")) {
            User admin = User.builder()
                    .email("admin@system.local")
                    .password(passwordEncoder.encode("ChangeMe123"))
                    .role(Role.ROLE_ADMIN)
                    .build();

            userService.createUser(admin);
            log.warn("Создан системный администратор по умолчанию");
            log.warn("Email: admin@system.local");
            log.warn("Пароль: ChangeMe123!");
        }
    }
}