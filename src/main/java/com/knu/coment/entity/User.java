package com.knu.coment.entity;

import com.knu.coment.global.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(unique = true, nullable = true)
    private String email;
    private Boolean notification;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role userRole;

    @Column(unique = true)
    private String githubId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserStack> userStacks = new HashSet<>();

    private String refreshToken;

    @Builder
    public User(String userName, String email, Boolean notification, Role userRole, String githubId, Set<UserStack> userStacks){
        this.userName = userName;
        this.email = email;
        this.notification = notification;
        this.userRole = userRole;
        this.githubId = githubId;
        this.userStacks = userStacks != null ? userStacks : new HashSet<>();
    }

    public User update(String email, Boolean notification, Set<UserStack> stacks) {
        if(email != null) this.email = email;
        if (notification != null) this.notification = notification;
        this.userStacks.clear();
        this.userStacks.addAll(stacks);
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

    public User updateGithub(String email, Boolean notification) {
        if(email != null) this.email = email;
        if (notification != null) this.notification = notification;
        return this;
    }
}
