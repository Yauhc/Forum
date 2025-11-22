package org.example.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ユーザー登録に使用するリクエスト DTO。
 */
@Data
public class UserRegisterDto {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 30, message = "ユーザー名は3〜30文字で入力してください")
    private String username;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が不正です")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, max = 16, message = "パスワードは6〜16文字で入力してください")
    @Pattern(regexp = "^[A-Za-z\\d@#$%^&+=!._-]+$",
            message = "パスワードは英数字と記号(@#$%^&+=!._-)のみ使用できます")
    private String password;
}
