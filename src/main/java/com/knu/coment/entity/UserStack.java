package com.knu.coment.entity;

import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@IdClass(UserStackId.class)
public class UserStack {

    @Id
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    private Stack stack;

    public UserStack(Long userId, Stack stack) {
        this.userId = userId;
        this.stack = stack;
    }
}
