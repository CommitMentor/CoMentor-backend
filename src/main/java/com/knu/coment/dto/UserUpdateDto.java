package com.knu.coment.dto;

import com.knu.coment.entity.User;
import com.knu.coment.entity.UserStack;
import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateDto {
    private String email;
    private boolean notification;
    private List<String> stackNames;

    public UserUpdateDto(String  email, boolean notification, List<String> stackNames) {
        this.email = email;
        this.notification = notification;
        this.stackNames = stackNames;
    }

//    public static UserUpdateDto fromEntity(User user) {
//        return new UserUpdateDto(user.getEmail(), user.getNotification(), user.getUserStacks());
//    }

}
