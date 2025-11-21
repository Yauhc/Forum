package org.example.project.security;

import lombok.Getter;
import org.example.project.entity.UserEntity;
import org.example.project.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security で利用するユーザーディテール。
 */
@Getter
public class ForumUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public ForumUserPrincipal(UserEntity userEntity) {
        this.userId = userEntity.getId() != null ? userEntity.getId().longValue() : null;
        this.username = userEntity.getUsername();
        this.password = userEntity.getPasswordHash();
        UserRole role = userEntity.getRole() != null ? userEntity.getRole() : UserRole.USER;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

