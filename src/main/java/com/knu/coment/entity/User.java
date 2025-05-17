package com.knu.coment.entity;

import com.knu.coment.global.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;
    @Column(unique = true)
    private String email;
    private Boolean notification;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(unique = true)
    private String githubId;

    private String refreshToken;
    private String githubAccessToken;

    @Column(name ="avatar_url")
    private String avatarUrl;

    private LocalDateTime lastActivityAt;

    @Builder
    public User(Long id, String userName, String email, Boolean notification, Role userRole, String githubId, String avatarUrl){
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.notification = notification;
        this.userRole = userRole;
        this.githubId = githubId;
        this.avatarUrl = avatarUrl;
    }

    public User update(String email, Boolean notification) {
        if (email != null) this.email = email;
        if (notification != null) this.notification = notification;
        return this;
    }

    public void updateRole(Role role) {
        this.userRole = role;
    }

    public String getRoleKey() {
        return this.userRole.getKey();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateGithubAccessToken(String githubAccessToken) {
        this.githubAccessToken = githubAccessToken;
    }
    public void updateLastActivityAt() {
        this.lastActivityAt = LocalDateTime.now();
    }

}
