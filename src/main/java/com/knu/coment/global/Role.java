package com.knu.coment.global;

import lombok.Getter;

@Getter
public enum Role {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    WITHDRAWN("ROLE_WITHDRAWN");

    private final String key;

    private Role(String key) {
        this.key = key;
    }
}
