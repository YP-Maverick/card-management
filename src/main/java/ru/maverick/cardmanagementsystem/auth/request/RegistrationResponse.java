package ru.maverick.cardmanagementsystem.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;

@Value
@Builder
@AllArgsConstructor
public class RegistrationResponse {

    Integer id;

    @Schema(description = "Email", example = "email@example.ex")
    String email;

    Role role;
}
