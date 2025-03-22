package com.knu.coment.entity;

import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Stack stackName;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    public UserStack(User user, Stack stackName) {
        this.user = user;
        this.stackName = stackName;
    }
}
