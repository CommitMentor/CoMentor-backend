package com.knu.coment.config.auth.dto;

import com.knu.coment.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * DB에서 User 엔티티를 불러온 뒤, Spring Security가 요구하는 UserDetails 형식으로 변환하는 클래스
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * 사용자의 권한 반환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 예시: User 엔티티에 Role이 있다고 가정, ROLE_ 접두어를 붙여 처리
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
    }

    /**
     * 사용자의 패스워드
     * - OAuth만 사용한다면 null or 빈 문자열로 둘 수도 있음
     */
    @Override
    public String getPassword() {
        // OAuth만 쓰는 경우라면 null 리턴
        return null;
    }

    /**
     * 사용자의 고유 식별자
     * - GitHub ID, 이메일 등 원하는 필드를 반환
     */
    @Override
    public String getUsername() {
        return user.getGithubId();
    }

    /**
     * 계정 만료 여부 (true면 만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠김 여부 (true면 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(패스워드) 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
