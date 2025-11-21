package org.example.project.security;

import org.example.project.dto.TokenPairDto;
import org.example.project.entity.UserEntity;

/**
 * JWT を発行・検証するサービス。
 */
public interface JwtTokenService {

    /**
     * アクセス・リフレッシュトークンのペアを生成する。
     */
    TokenPairDto generateTokenPair(UserEntity user);

    /**
     * トークンを検証し、ユーザー ID を返す。
     */
    Long parseUserId(String token);

    /**
     * トークン種別を取得する。
     */
    TokenType resolveTokenType(String token);

    /**
     * トークンの有効期限（秒）を返す。
     */
    long getAccessTokenTtl();

    long getRefreshTokenTtl();
}

