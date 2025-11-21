package org.example.project.service.impl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.SessionConstants;
import org.example.project.common.error.ErrorCode;
import org.example.project.common.exception.BusinessException;
import org.example.project.dto.LoginResponseDto;
import org.example.project.dto.TokenPairDto;
import org.example.project.dto.UserLoginDto;
import org.example.project.dto.UserLoginForm;
import org.example.project.dto.UserProfileDto;
import org.example.project.dto.UserRegisterDto;
import org.example.project.entity.UserEntity;
import org.example.project.entity.UserRole;
import org.example.project.security.JwtTokenService;
import org.example.project.security.TokenType;
import org.example.project.service.AuthService;
import org.example.project.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認証サービス実装。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileDto register(UserRegisterDto registerDto) {
        if (userService.findByUsername(registerDto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userService.findByEmail(registerDto.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(UserRole.USER);
        boolean saved = userService.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ユーザー登録に失敗しました");
        }
        log.info("user registered: {}", user.getUsername());
        return UserProfileDto.fromEntity(user);
    }

    @Override
    public LoginResponseDto login(UserLoginDto loginDto, HttpSession session) {
        return doLogin(loginDto.getUsername(), loginDto.getPassword(), session);
    }

    @Override
    public LoginResponseDto login(UserLoginForm loginForm, HttpSession session) {
        return doLogin(loginForm.getUsername(), loginForm.getPassword(), session);
    }

    @Override
    public TokenPairDto refreshToken(String refreshToken) {
        TokenType type = jwtTokenService.resolveTokenType(refreshToken);
        if (type != TokenType.REFRESH) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "リフレッシュトークンを指定してください");
        }
        Long userId = jwtTokenService.parseUserId(refreshToken);
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return jwtTokenService.generateTokenPair(user);
    }

    private LoginResponseDto doLogin(String username, String password, HttpSession session) {
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        TokenPairDto tokenPair = jwtTokenService.generateTokenPair(user);
        UserProfileDto profile = UserProfileDto.fromEntity(user);
        session.setAttribute(SessionConstants.SESSION_USER_KEY, profile);
        log.info("user login success: {}", user.getUsername());
        return new LoginResponseDto(profile, tokenPair);
    }
}

