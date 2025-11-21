package org.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * トークンリフレッシュ用 DTO。
 */
@Data
public class TokenRefreshRequest {

    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;
}

