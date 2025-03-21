package com.knu.coment.dto;

import com.knu.coment.entity.UserStack;
import com.knu.coment.global.Role;
import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateDto {
    private String email;
    private boolean notification;
    private List<String> stackNames; ;

    public UserUpdateDto(String email, boolean notification, List<UserStack> userStacks, Role userRole) {
        this.email = email;
        this.notification = notification;
        this.stackNames = stackNames;
    }

}
