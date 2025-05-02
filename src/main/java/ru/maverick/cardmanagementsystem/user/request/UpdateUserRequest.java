package ru.maverick.cardmanagementsystem.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;

@Value
@Builder
public class UpdateUserRequest {

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,32}$",
            message = "Пароль должен содержать от 8 до 32 символов, буквы и цифры.")
    String password;

    @NotBlank
    String fistName;
    @NotBlank
    String lastName;

    Role role;
}
