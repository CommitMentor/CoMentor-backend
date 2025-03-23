package com.knu.coment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class UserUpdateDto {
    @NotNull(message = "email cannot be null")
    @Schema(description = "사용자 이메일", example = "example@gmail.com")
    private String email;
    @NotNull(message = "notification cannot be null")
    @Schema(description = "알림 설정", example = "true")
    private boolean notification;
    @NotNull(message = "stackNames cannot be null")
    @Schema(description = "사용자 스택 배열", example = "FRONTEND, BACKEND")
    private List<String> stackNames;

    public UserUpdateDto(String  email, boolean notification, List<String> stackNames) {
        this.email = email;
        this.notification = notification;
        this.stackNames = stackNames;
    }
}
