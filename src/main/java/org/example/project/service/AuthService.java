package org.example.project.service;

import jakarta.servlet.http.HttpSession;
import org.example.project.dto.LoginResponseDto;
import org.example.project.dto.TokenPairDto;
import org.example.project.dto.UserLoginDto;
import org.example.project.dto.UserLoginForm;
import org.example.project.dto.UserProfileDto;
import org.example.project.dto.UserRegisterDto;

/**
 * 認証系サービス。
 */
public interface AuthService {

    /**
     * ユーザー登録処理。
     */
    UserProfileDto register(UserRegisterDto registerDto);

    /**
     * ログイン処理（JSON）。
     */
    LoginResponseDto login(UserLoginDto loginDto, HttpSession session);

    /**
     * ログイン処理（フォーム）。
     */
    LoginResponseDto login(UserLoginForm loginForm, HttpSession session);

    /**
     * リフレッシュトークンを使って新しいトークンを発行する。
     */
    TokenPairDto refreshToken(String refreshToken);
}

