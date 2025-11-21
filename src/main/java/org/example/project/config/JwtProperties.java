package org.example.project.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT に関する設定値。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "myforum.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT シークレットを設定してください")
    private String secret;

    @NotBlank(message = "JWT issuer を設定してください")
    private String issuer;

    @Min(value = 60, message = "アクセストークン有効期限は 60 秒以上で設定してください")
    private long accessTokenExpiresInSeconds;

    @Min(value = 300, message = "リフレッシュトークン有効期限は 5 分以上で設定してください")
    private long refreshTokenExpiresInSeconds;
}

