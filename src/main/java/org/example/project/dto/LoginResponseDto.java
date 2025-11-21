package org.example.project.dto;

import lombok.Value;

/**
 * ログイン成功時に返却する DTO。
 */
@Value
public class LoginResponseDto {
    UserProfileDto user;
    TokenPairDto tokenPair;
}

