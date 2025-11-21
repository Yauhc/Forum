package org.example.project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.error.ErrorCode;
import org.example.project.common.exception.BusinessException;
import org.example.project.config.JwtProperties;
import org.example.project.dto.TokenPairDto;
import org.example.project.entity.UserEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT トークン発行・検証処理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT シークレットは 32 バイト以上にしてください");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public TokenPairDto generateTokenPair(UserEntity user) {
        String accessToken = buildToken(user, TokenType.ACCESS, jwtProperties.getAccessTokenExpiresInSeconds());
        String refreshToken = buildToken(user, TokenType.REFRESH, jwtProperties.getRefreshTokenExpiresInSeconds());
        return TokenPairDto.of(accessToken, jwtProperties.getAccessTokenExpiresInSeconds(),
                refreshToken, jwtProperties.getRefreshTokenExpiresInSeconds());
    }

    @Override
    public Long parseUserId(String token) {
        try {
            Jws<Claims> jws = buildParser().parseSignedClaims(token);
            return Long.parseLong(jws.getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("invalid token: {}", ex.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID, ErrorCode.TOKEN_INVALID.getDefaultMessage());
        }
    }

    @Override
    public TokenType resolveTokenType(String token) {
        try {
            Jws<Claims> jws = buildParser().parseSignedClaims(token);
            return TokenType.valueOf(jws.getPayload().get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("token type resolve failed: {}", ex.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID, ErrorCode.TOKEN_INVALID.getDefaultMessage());
        }
    }

    @Override
    public long getAccessTokenTtl() {
        return jwtProperties.getAccessTokenExpiresInSeconds();
    }

    @Override
    public long getRefreshTokenTtl() {
        return jwtProperties.getRefreshTokenExpiresInSeconds();
    }

    private String buildToken(UserEntity user, TokenType tokenType, long ttlSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .claim("username", user.getUsername())
                .claim("role", user.getRole() != null ? user.getRole().name() : null)
                .claim("tokenType", tokenType.name())
                .signWith(secretKey)
                .compact();
    }

    private Jwts.Parser buildParser() {
        return Jwts.parser().verifyWith(secretKey).requireIssuer(jwtProperties.getIssuer()).build();
    }
}

