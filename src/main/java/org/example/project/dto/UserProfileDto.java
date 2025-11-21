package org.example.project.dto;

import lombok.Builder;
import lombok.Value;
import org.example.project.entity.UserEntity;
import org.example.project.entity.UserRole;

/**
 * クライアントへ公開するユーザープロファイル DTO。
 */
@Value
@Builder
public class UserProfileDto {
    Long id;
    String username;
    String email;
    String avatarUrl;
    String bio;
    UserRole role;

    /**
     * エンティティから DTO へ変換する。
     */
    public static UserProfileDto fromEntity(UserEntity entity) {
        return UserProfileDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .bio(entity.getBio())
                .role(entity.getRole())
                .build();
    }
}

