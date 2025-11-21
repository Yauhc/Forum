package org.example.project.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * セキュリティ補助エンドポイント。
 */
@RestController
public class SecurityController {

    /**
     * CSRF トークンを取得する。
     */
    @GetMapping("/csrf-token")
    public Map<String, String> csrfToken(CsrfToken token) {
        return Map.of("token", token.getToken());
    }
}

