package com.knu.coment.service;

import com.knu.coment.config.auth.dto.OAuthAttributes;
import com.knu.coment.dto.UserDto;
import com.knu.coment.entity.UserStack;
import com.knu.coment.exception.UserExceptionHandler;
import com.knu.coment.exception.code.UserErrorCode;
import com.knu.coment.entity.User;
import com.knu.coment.global.Role;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public User findByGithubId(String githubId) {
        return userRepository.findByGithubIdFetchStacks(githubId)
                .orElseThrow(() -> new UserExceptionHandler(UserErrorCode.NOT_FOUND_USER));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User saveOrUpdateGithub(OAuthAttributes attributes) {
        User user = userRepository.findByGithubIdFetchStacks(attributes.getGithubId())
                .orElseGet(() -> attributes.toEntity());
        return userRepository.save(user);
    }


    public User join(String githubId, UserDto dto) {
        User user = findByGithubId(githubId);
        if (user.getUserRole() == Role.USER) {
            throw new UserExceptionHandler(UserErrorCode.ALREADY_JOINED_USER);
        }
        Set<UserStack> stacks = dto.getStackNames().stream()
                .map(name -> new UserStack(user, Stack.valueOf(name)))
                .collect(Collectors.toSet());

        user.update(dto.getEmail(), dto.isNotification(), stacks);
        user.updateRole(Role.USER);

        return userRepository.save(user);
    }


    public User renewRefreshToken(String githubId) {
        User user = findByGithubId(githubId);
        if(user == null || user.getRefreshToken() == null) {
            throw new UserExceptionHandler(UserErrorCode.MISSING_REQUIRED_FIELD);
        }

        if (!jwtTokenProvider.validateToken(user.getRefreshToken())) {
            throw new UserExceptionHandler(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newRefreshToken = jwtTokenProvider.createRefreshToken(githubId);
        user.updateRefreshToken(newRefreshToken);
        return userRepository.save(user);
    }

    public UserDto getUserInfo(String githubId) {
        User user = findByGithubId(githubId);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public User updateInfo(String githubId, UserDto userDto) {
        User user = findByGithubId(githubId);
        Set<UserStack> stacks = userDto.getStackNames().stream()
                .map(name -> new UserStack(user, Stack.valueOf(name)))
                .collect(Collectors.toSet());
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("스택 정보는 최소 하나 이상 입력되어야 합니다.");
        }
        user.update(userDto.getEmail(), userDto.isNotification(), stacks);

        return userRepository.save(user);
    }

    public User withdrawn(String githubId) {
        User user = findByGithubId(githubId);
        user.updateRole(Role.valueOf("WITHDRAWN"));
        return userRepository.save(user);
    }
}
