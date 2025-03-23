package com.knu.coment.entity;

import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
public class UserStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "stack_name", nullable = false)
    private Stack stackName;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    public UserStack(User user, Stack stackName) {
        this.user = user;
        this.stackName = stackName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStack userStack = (UserStack) o;
        return stackName == userStack.stackName && Objects.equals(user, userStack.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, stackName);
    }
}
