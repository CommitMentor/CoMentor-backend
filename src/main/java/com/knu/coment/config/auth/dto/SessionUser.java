package com.knu.coment.config.auth.dto;

import com.knu.coment.entity.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String name;
    private String githubId;

    public SessionUser(User user) {
        this.name = user.getUserName();
        this.githubId = user.getGithubId();
    }
}
