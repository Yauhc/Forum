package org.example.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 投稿作成リクエスト DTO。
 */
@Data
public class PostCreateRequest {
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @NotBlank(message = "本文は必須です")
    @Size(max = 10000, message = "本文は10000文字以内で入力してください")
    private String content;

    private Long forumId;
}

