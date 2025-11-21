package org.example.project.dto;

import lombok.Value;

/**
 * アクセストークンとリフレッシュトークンのセット。
 */
@Value
public class TokenPairDto {
    String accessToken;
    long accessTokenExpiresIn;
    String refreshToken;
    long refreshTokenExpiresIn;

    public static TokenPairDto of(String accessToken, long accessTokenExpiresIn,
                                  String refreshToken, long refreshTokenExpiresIn) {
        return new TokenPairDto(accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn);
    }
}

