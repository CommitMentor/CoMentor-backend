package com.knu.coment.service;

import com.knu.coment.config.auth.dto.OAuthAttributes;
import com.knu.coment.dto.UserDto;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.UserStack;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.UserException;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.exception.code.UserErrorCode;
import com.knu.coment.entity.User;
import com.knu.coment.global.Role;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.FolderRepository;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.repository.UserStackRepository;
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
    private final FolderRepository folderRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserStackRepository userStackRepository;

    public User findByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
    }

    public void saveUser(User user) {
         userRepository.save(user);
    }

    public User saveOrUpdateGithub(OAuthAttributes attributes) {
        User user = userRepository.findByGithubId(attributes.getGithubId())
                .orElseGet(() -> attributes.toEntity());
        return userRepository.save(user);
    }


    public User join(String githubId, UserDto dto) {
        User user = findByGithubId(githubId);
        if (user.getUserRole() == Role.USER) {
            throw new UserException(UserErrorCode.ALREADY_JOINED_USER);
        }
        user.update(dto.getEmail(), dto.isNotification());
        user.updateRole(Role.USER);
        Set<UserStack> stacks = dto.getStackNames().stream()
                .map(name -> new UserStack(user.getId(), Stack.valueOf(name)))
                .collect(Collectors.toSet());
        userStackRepository.saveAll(stacks);
        Folder folder = new Folder( "default", user.getId());
        folderRepository.save(folder);
        return userRepository.save(user);
    }


    public User renewRefreshToken(String githubId) {
        User user = findByGithubId(githubId);
        if(user == null || user.getRefreshToken() == null) {
            throw new UserException(UserErrorCode.MISSING_REQUIRED_FIELD);
        }
        if (!jwtTokenProvider.validateToken(user.getRefreshToken())) {
            throw new UserException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }
        String newRefreshToken = jwtTokenProvider.createRefreshToken(githubId);
        user.updateRefreshToken(newRefreshToken);
        return userRepository.save(user);
    }

    public UserDto getUserInfo(String githubId) {
        User user = findByGithubId(githubId);
        Set<UserStack> userStacks = userStackRepository.findAllByUserId(user.getId());
        return UserDto.fromEntity(user, userStacks);
    }

    @Transactional
    public User updateInfo(String githubId, UserDto userDto) {
        User user = findByGithubId(githubId);
        userStackRepository.deleteAllByUserId(user.getId());
        Set<UserStack> stacks = userDto.getStackNames().stream()
                .map(name -> new UserStack(user.getId(), Stack.valueOf(name)))
                .collect(Collectors.toSet());
        if (stacks.isEmpty()) {
            throw new ProjectException(ProjectErrorCode.INVALID_RROJECT_STACK);
        }
        userStackRepository.saveAll(stacks);
        user.update(userDto.getEmail(), userDto.isNotification());
        return userRepository.save(user);
    }


    public User withdrawn(String githubId) {
        User user = findByGithubId(githubId);
        user.updateRole(Role.valueOf("WITHDRAWN"));
        return userRepository.save(user);
    }
}
