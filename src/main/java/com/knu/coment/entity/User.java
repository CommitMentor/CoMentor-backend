package com.knu.coment.entity;

import com.knu.coment.global.Role;
import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String userName;
    @Column(unique = true, nullable = true)
    private String email;
    private Boolean notification;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(unique = true)
    private String githubId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserStack> userStacks;

    private String refreshToken;

    @Builder
    public User(String userName, String email, Boolean notification, Role userRole, String githubId, List<UserStack> userStacks){
        this.userName = userName;
        this.email = email;
        this.notification = notification;
        this.userRole = userRole;
        this.githubId = githubId;
        this.userStacks = userStacks;

    }

    public User update(String email, Boolean notification, List<UserStack> userStacks) {
        this.notification = notification;
        this.email = email;
        this.userStacks = userStacks;
        return this;
    }

    public void updateRole(Role role) {
        this.userRole = role;
    }

    public String getRoleKey() {
        return this.userRole.getKey();
    }

    public User creatGithub(String name, String githubId) {
        this.userName = name;
        this.githubId = githubId;
        return this;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
