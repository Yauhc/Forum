package org.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * フォーム送信で利用するログイン DTO。
 */
@Data
public class UserLoginForm {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 2, max = 30, message = "ユーザー名は2〜30文字で入力してください")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 64, message = "パスワードは8〜64文字で入力してください")
    private String password;
}

