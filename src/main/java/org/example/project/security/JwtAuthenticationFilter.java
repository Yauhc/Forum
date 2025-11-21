package org.example.project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.error.ErrorCode;
import org.example.project.common.exception.BusinessException;
import org.example.project.entity.UserEntity;
import org.example.project.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authorization ヘッダーから JWT を検証するフィルター。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                TokenType tokenType = jwtTokenService.resolveTokenType(token);
                if (tokenType != TokenType.ACCESS) {
                    throw new BusinessException(ErrorCode.TOKEN_INVALID, "アクセストークンを指定してください");
                }
                Long userId = jwtTokenService.parseUserId(token);
                UserEntity user = userService.getById(userId);
                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    ForumUserPrincipal principal = new ForumUserPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (BusinessException ex) {
                log.warn("JWT 検証失敗: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}

