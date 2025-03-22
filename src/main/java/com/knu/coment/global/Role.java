package com.knu.coment.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST,
    USER,
    WITHDRAWN;

    public String getKey() {
        return name();
    }
}
