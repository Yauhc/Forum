package org.example.project.entity;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * ユーザーの権限種別。
 */
public enum UserRole implements IEnum<String> {
    USER,
    ADMIN,
    MODERATOR;

    @Override
    public String getValue() {
        return name();
    }
}