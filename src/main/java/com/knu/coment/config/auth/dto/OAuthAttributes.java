package com.knu.coment.config.auth.dto;

import com.knu.coment.global.Role;
import com.knu.coment.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String githubId;
    private String avatarUrl;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String githubId, String avatarUrl) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.githubId = githubId;
        this.avatarUrl = avatarUrl;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGithub(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGithub(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .githubId(String.valueOf(attributes.get("id")))
                .avatarUrl((String) attributes.get("avatar_url"))
                .build();
    }

    public User toEntity() {
        return User.builder()
                .userName(name)
                .githubId(githubId)
                .notification(false)
                .userRole(Role.GUEST)
                .avatarUrl(avatarUrl)
                .build();
    }
}
