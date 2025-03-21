package com.knu.coment.service;

import com.knu.coment.config.auth.dto.OAuthAttributes;
import com.knu.coment.dto.UserUpdateDto;
import com.knu.coment.entity.UserStack;
import com.knu.coment.exception.UserExceptionHandler;
import com.knu.coment.exception.code.UserErrorCode;
import com.knu.coment.entity.User;
import com.knu.coment.global.Role;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("해당 깃허브ID 사용자가 없습니다"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User saveOrUpdateGithub(OAuthAttributes attributes) {
        User user = userRepository.findByGithubId(attributes.getGithubId())
                .map(entity -> entity.update(attributes.getEmail(), entity.getNotification(), entity.getUserStacks())) // 기존 데이터가 있다면 업데이트
                .orElseGet(() -> attributes.toEntity());

        return userRepository.save(user);
    }


    public User join(Long userId, UserUpdateDto userUpdateDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserExceptionHandler(UserErrorCode.INVALID_USER_ID));
        List<UserStack> userStacks = userUpdateDto.getStackNames().stream()
                .map(stackName -> new UserStack(user, Stack.valueOf(stackName)))
                .collect(Collectors.toList());

        user.update(userUpdateDto.getEmail(), userUpdateDto.isNotification(), userStacks);
        user.updateRole(Role.valueOf("USER"));
        return userRepository.save(user);
    }

}
