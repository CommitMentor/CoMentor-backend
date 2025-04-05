package com.knu.coment.dto;

import com.knu.coment.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserDto {
    @NotNull(message = "email cannot be null")
    @Schema(description = "사용자 이메일", example = "example@gmail.com")
    private String email;
    @NotNull(message = "notification cannot be null")
    @Schema(description = "알림 설정", example = "true")
    private boolean notification;
    @NotNull(message = "stackNames cannot be null")
    @Schema(description = "사용자 스택 배열", example = "[\"FRONTEND\", \"BACKEND\",\"DB\",\"ALGORITHM\"]")
    private Set<@NotBlank(message = "stack name cannot be blank") String> stackNames;
    @Schema(description = "사용자 이미지", example = "https://``")
    private String avatarUrl;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    public UserDto(String  email, boolean notification, Set<String> stackNames, String avatarUrl, String userName) {
        this.email = email;
        this.notification = notification;
        this.stackNames = stackNames;
        this.avatarUrl = avatarUrl;
        this.userName = userName;
    }
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getEmail(),
                user.getNotification(),
                user.getUserStacks().stream()
                        .map(userStack -> userStack.getStackName().name())
                        .collect(Collectors.toSet()),
                user.getAvatarUrl(),
                user.getUserName()
        );
    }
}
