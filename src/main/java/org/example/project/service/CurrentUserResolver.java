package org.example.project.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.project.common.SessionConstants;
import org.example.project.dto.UserProfileDto;
import org.example.project.entity.UserEntity;
import org.example.project.security.ForumUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 現在のログインユーザーを取得するヘルパー。
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserService userService;

    public Optional<UserProfileDto> resolve(HttpSession session) {
        Object sessionUser = session.getAttribute(SessionConstants.SESSION_USER_KEY);
        if (sessionUser instanceof UserProfileDto dto) {
            return Optional.of(dto);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ForumUserPrincipal principal) {
            UserEntity user = userService.getById(principal.getUserId());
            if (user != null) {
                UserProfileDto profile = UserProfileDto.fromEntity(user);
                session.setAttribute(SessionConstants.SESSION_USER_KEY, profile);
                return Optional.of(profile);
            }
        }
        return Optional.empty();
    }
}

