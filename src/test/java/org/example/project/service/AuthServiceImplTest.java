package org.example.project.service;

import org.example.project.common.SessionConstants;
import org.example.project.common.exception.BusinessException;
import org.example.project.dto.LoginResponseDto;
import org.example.project.dto.TokenPairDto;
import org.example.project.dto.UserLoginDto;
import org.example.project.dto.UserRegisterDto;
import org.example.project.entity.UserEntity;
import org.example.project.entity.UserRole;
import org.example.project.security.JwtTokenService;
import org.example.project.security.TokenType;
import org.example.project.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthServiceImpl の単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("Password1");

        when(userService.findByUsername("alice")).thenReturn(null);
        when(userService.findByEmail("alice@test.com")).thenReturn(null);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userService.save(any(UserEntity.class))).thenReturn(true);

        var profile = authService.register(dto);

        assertThat(profile.getUsername()).isEqualTo("alice");
        verify(passwordEncoder).encode("Password1");
        verify(userService).save(any(UserEntity.class));
    }

    @Test
    void register_throws_whenUsernameExists() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("Password1");

        when(userService.findByUsername("alice")).thenReturn(new UserEntity());

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void login_success_setsSessionAndTokens() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("alice");
        loginDto.setPassword("Password1");

        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("alice");
        entity.setPasswordHash("hashed");
        entity.setRole(UserRole.USER);

        when(userService.findByUsername("alice")).thenReturn(entity);
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);
        TokenPairDto pair = TokenPairDto.of("access", 1000, "refresh", 2000);
        when(jwtTokenService.generateTokenPair(entity)).thenReturn(pair);

        MockHttpSession session = new MockHttpSession();
        LoginResponseDto response = authService.login(loginDto, session);

        assertThat(response.getTokenPair()).isEqualTo(pair);
        assertThat(session.getAttribute(SessionConstants.SESSION_USER_KEY)).isNotNull();
    }

    @Test
    void login_throws_whenPasswordInvalid() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("alice");
        loginDto.setPassword("Password1");

        UserEntity entity = new UserEntity();
        entity.setPasswordHash("hashed");
        when(userService.findByUsername("alice")).thenReturn(entity);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginDto, new MockHttpSession()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refreshToken_success() {
        String refreshToken = "refresh";
        when(jwtTokenService.resolveTokenType(refreshToken)).thenReturn(TokenType.REFRESH);
        when(jwtTokenService.parseUserId(refreshToken)).thenReturn(1L);
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("alice");
        when(userService.getById(1L)).thenReturn(entity);
        TokenPairDto pair = TokenPairDto.of("a", 1, "r", 2);
        when(jwtTokenService.generateTokenPair(entity)).thenReturn(pair);

        TokenPairDto result = authService.refreshToken(refreshToken);

        assertThat(result).isEqualTo(pair);
        verify(jwtTokenService).generateTokenPair(entity);
    }

    @Test
    void refreshToken_rejectsAccessToken() {
        when(jwtTokenService.resolveTokenType("access")).thenReturn(TokenType.ACCESS);
        assertThatThrownBy(() -> authService.refreshToken("access"))
                .isInstanceOf(BusinessException.class);
    }
}

