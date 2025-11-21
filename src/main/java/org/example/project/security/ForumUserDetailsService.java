package org.example.project.security;

import lombok.RequiredArgsConstructor;
import org.example.project.common.error.ErrorCode;
import org.example.project.common.exception.BusinessException;
import org.example.project.entity.UserEntity;
import org.example.project.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用の UserDetailsService。
 */
@Service
@RequiredArgsConstructor
public class ForumUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return new ForumUserPrincipal(user);
    }
}

