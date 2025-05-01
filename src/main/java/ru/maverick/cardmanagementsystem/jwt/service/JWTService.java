package ru.maverick.cardmanagementsystem.jwt.service;

import ru.maverick.cardmanagementsystem.exception.NotFoundException;
import ru.maverick.cardmanagementsystem.exception.TokenExpiredException;
import ru.maverick.cardmanagementsystem.jwt.model.Token;
import ru.maverick.cardmanagementsystem.jwt.repository.JWTRepository;
import ru.maverick.cardmanagementsystem.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.access.secret-key}")
    private String accessSecretKey;

    @Value("${jwt.access.duration}")
    private long accessTokenDuration;

    @Value("${jwt.refresh.duration}")
    private long refreshTokenDuration;

    private final JWTRepository jwtRepository;

    public String generateAccessToken(
            String username,
            Integer userId
    ) {
        return Jwts.builder()
                .claim("user_id", userId.toString())
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenDuration))
                .signWith(getAccessKey())
                .compact();
    }

    public String generateRefreshToken(
            String username,
            Integer userId
    ) {
        return Jwts.builder()
                .claim("user_id", userId.toString())
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenDuration))
                .signWith(getAccessKey())
                .compact();
    }

    private SecretKey getAccessKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("user_id", Long.class));
    }

    public boolean validateAccessToken(String token,
                                       UserDetails userDetails
    ) {
        final String userName = extractUserName(token);

        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateRefreshToken(String token,
                                        UserDetails userDetails
    ) {
        String userName = extractUserName(token);
        Token storedToken = jwtRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh-токен не найден в базе данных"));

        if (isTokenExpired(token)) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            jwtRepository.save(storedToken);

            throw new TokenExpiredException("Token expired");
        }

        return userName.equals(userDetails.getUsername()) && !storedToken.isRevoked();
    }

    public void revokeAllUserRefreshTokens(User user) {
        List<Token> userTokens = jwtRepository.findAllByUser(user)
                .stream()
                .filter(token -> token.isRefresh()
                        && !token.isRevoked()
                        && !token.expired)
                .toList();

        userTokens.forEach(token -> token.setRevoked(true));

        jwtRepository.saveAll(userTokens);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public void saveRefreshToken(String refreshToken) {
        jwtRepository.save(
                Token.builder()
                        .token(refreshToken)
                        .isRefresh(true)
                        .expired(false)
                        .revoked(false)
                        .expiresAt(new Date(System.currentTimeMillis() + refreshTokenDuration))
                        .build()
        );
    }

    public String createRefreshToken(String username, Integer userId) {
        String refreshToken = generateRefreshToken(username, userId);
        saveRefreshToken(refreshToken);
        return refreshToken;
    }

    public long getCountActiveRefreshTokenByUser(User user) {
        return jwtRepository.countByUserAndRevokedFalseAndExpiredFalse(user);
    }

    public void revokeRefreshToken(String refreshToken) {
        Token token = jwtRepository.findByToken(refreshToken)
                .orElseThrow(() -> new NotFoundException("Токен не найден"));

        token.setRevoked(true);
        jwtRepository.save(token);
    }

}