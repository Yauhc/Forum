package org.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * JSON ログイン要求用 DTO。
 */
@Data
public class UserLoginDto {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 2, max = 30, message = "ユーザー名は2〜30文字で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 64, message = "パスワードは8〜64文字で入力してください")
    private String password;
}

