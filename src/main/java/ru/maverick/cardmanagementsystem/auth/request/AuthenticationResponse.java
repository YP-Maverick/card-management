package ru.maverick.cardmanagementsystem.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Value
@Builder
@AllArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("refresh_token")
    String refreshToken;
}
