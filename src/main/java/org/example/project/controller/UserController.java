package org.example.project.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.R;
import org.example.project.common.SessionConstants;
import org.example.project.dto.LoginResponseDto;
import org.example.project.dto.TokenPairDto;
import org.example.project.dto.TokenRefreshRequest;
import org.example.project.dto.UserLoginDto;
import org.example.project.dto.UserLoginForm;
import org.example.project.dto.UserProfileDto;
import org.example.project.dto.UserRegisterDto;
import org.example.project.entity.UserEntity;
import org.example.project.service.AuthService;
import org.example.project.service.CurrentUserResolver;
import org.example.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ユーザー関連 API。
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;

    /**
     * ユーザー登録。
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<R> register(@Valid @RequestBody UserRegisterDto registerDto) {
        UserProfileDto profile = authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(R.ok().put("user", profile));
    }

    /**
     * JSON ログイン。
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<R> loginJson(@Valid @RequestBody UserLoginDto loginDto, HttpSession session) {
        LoginResponseDto response = authService.login(loginDto, session);
        return buildLoginResponse(response);
    }

    /**
     * form-urlencoded ログイン（従来互換）。
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<R> loginForm(@Validated UserLoginForm loginForm, HttpSession session) {
        LoginResponseDto response = authService.login(loginForm, session);
        return buildLoginResponse(response);
    }

    /**
     * トークンリフレッシュ。
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<R> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenPairDto pair = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(R.ok().put("tokens", pair));
    }

    /**
     * ID 指定でユーザー取得。
     */
    @GetMapping("/{id}")
    public ResponseEntity<R> getUser(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(R.error(404, "ユーザーが存在しません"));
        }
        return ResponseEntity.ok(R.ok().put("user", UserProfileDto.fromEntity(user)));
    }

    /**
     * 現在ログイン中ユーザーを取得。
     */
    @GetMapping("/me")
    public ResponseEntity<R> me(HttpSession session) {
        var currentUser = currentUserResolver.resolve(session);
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(R.error(401, "ログインしてください"));
        }
        return ResponseEntity.ok(R.ok().put("user", currentUser.get()));
    }

    /**
     * ログアウト。
     */
    @PostMapping("/logout")
    public ResponseEntity<R> logout(HttpSession session) {
        session.removeAttribute(SessionConstants.SESSION_USER_KEY);
        session.invalidate();
        return ResponseEntity.ok(R.ok("ログアウトしました"));
    }

    private ResponseEntity<R> buildLoginResponse(LoginResponseDto response) {
        return ResponseEntity.ok(R.ok()
                .put("user", response.getUser())
                .put("tokens", response.getTokenPair())
                .put("redirectUrl", "/myforum.html"));
    }
}
