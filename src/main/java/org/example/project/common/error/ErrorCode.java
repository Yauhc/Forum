package org.example.project.common.error;

/**
 * エラーコードとユーザ向けメッセージを集約する列挙体。
 */
public enum ErrorCode {
    SUCCESS(200, "成功"),
    USERNAME_EXISTS(1001, "ユーザー名は既に使用されています"),
    EMAIL_EXISTS(1002, "メールアドレスは既に使用されています"),
    USER_NOT_FOUND(1003, "ユーザーが見つかりません"),
    INVALID_CREDENTIALS(1004, "認証に失敗しました"),
    TOKEN_EXPIRED(1005, "トークンの有効期限が切れています"),
    TOKEN_INVALID(1006, "不正なトークンです"),
    VALIDATION_FAILED(1007, "入力値が不正です"),
    UNAUTHORIZED(1008, "認証が必要です"),
    FORBIDDEN(1009, "権限がありません"),
    SYSTEM_ERROR(1500, "システムエラーが発生しました");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

