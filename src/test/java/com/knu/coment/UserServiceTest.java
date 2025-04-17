package com.knu.coment;

import com.knu.coment.dto.UserDto;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserStack;
import com.knu.coment.exception.UserExceptionHandler;
import com.knu.coment.exception.code.UserErrorCode;
import com.knu.coment.global.Role;
import com.knu.coment.global.Stack;
import com.knu.coment.repository.FolderRepository;
import com.knu.coment.repository.UserRepository;
import com.knu.coment.repository.UserStackRepository;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // 설명: MockitoExtension을 사용하여 테스트 확장
class UserServiceTest {

    @Mock // 설명: Mock 객체 생성
    private UserRepository userRepository;
    @Mock
    private UserStackRepository userStackRepository;
    @Mock
    private FolderRepository folderRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test.@example.com")
                .notification(true)
                .userRole(Role.GUEST)
                .githubId("testGithubId")
                .avatarUrl("https://example.com/avatar.png")
                .userName("test")
                .build();
        Set<String> stackNames = new HashSet<>();
        stackNames.add("FRONTEND");
        stackNames.add("BACKEND");
        userDto = new UserDto("test.@example.com", true, stackNames, "https://example.com/avatar.png", "test");

    }

    @Test
    @DisplayName("findByGithubId - 정상적으로 유저를 조회할 수 있어야 한다")
    void testFindByGithubId() {
        // given
        given(userRepository.findByGithubId("testGithubId"))
                .willReturn(Optional.of(user));
        // when
        User result = userService.findByGithubId("testGithubId");
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGithubId()).isEqualTo("testGithubId");
        //userRepository 메서드가 1회 호출되었는지 검증
        verify(userRepository).findByGithubId("testGithubId");
    }

    @Test
    @DisplayName("findByGithubId - 유저를 조회할 수 없을 경우 예외가 발생해야 한다")
    void testFindByGithub_Fail_UserNotFound() {
        // given: "notExist" 인자로 호출 시, 빈 Optional을 반환하도록 스텁 설정
        given(userRepository.findByGithubId("notExist"))
                .willReturn(Optional.empty());
        // when: "notExist" 인자로 메서드 호출
        assertThatThrownBy(() -> userService.findByGithubId("notExist"))
                .isInstanceOf(UserExceptionHandler.class)
                .hasMessageContaining(UserErrorCode.NOT_FOUND_USER.getMessage());
        // then: "notExist" 인자로 호출되었음을 검증
        verify(userRepository, times(1)).findByGithubId("notExist");
    }

    @Test
    @DisplayName("join - 정상적으로 유저가 가입되어야 한다")
    void testJoin_Success() {
        // given
        given(userRepository.findByGithubId("testGithubId"))
                .willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(user);

        // userStack 저장 동작도 mocking
        given(userStackRepository.saveAll(anySet())).willReturn(List.of(
                new UserStack(user.getId(), Stack.FRONTEND),
                new UserStack(user.getId(), Stack.BACKEND)
        ));
        given(userStackRepository.findAllByUserId(user.getId()))
                .willReturn(Set.of(
                        new UserStack(user.getId(), Stack.FRONTEND),
                        new UserStack(user.getId(), Stack.BACKEND)
                ));

        // when
        User joinedUser = userService.join("testGithubId", userDto);
        Set<UserStack> userStacks = userStackRepository.findAllByUserId(joinedUser.getId());

        // then
        assertThat(joinedUser.getEmail()).isEqualTo("test.@example.com");
        assertThat(joinedUser.getNotification()).isTrue();

        List<String> actualStackNames = userStacks.stream()
                .map(UserStack::getStack)
                .map(Enum::name)
                .collect(Collectors.toList());

        assertThat(actualStackNames)
                .containsExactlyInAnyOrder("FRONTEND", "BACKEND");

        verify(userRepository, times(1)).findByGithubId("testGithubId");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userStackRepository, times(1)).saveAll(anySet());
        verify(userStackRepository, times(1)).findAllByUserId(user.getId());
        verify(folderRepository, times(1)).save(any(Folder.class));
    }


    @Test
    @DisplayName("join - 이미 Role.USER인 유저는 ALREADY_JOINED_USER 예외 발생")
    void testJoin_AlreadyJoinedUser() {
        // (23) 이미 USER 권한으로 가입된 유저 세팅
        user.updateRole(Role.USER);
        // (24) 리포지토리에서 user 반환
        given(userRepository.findByGithubId("testGithubId"))
                .willReturn(Optional.of(user));

        // (25) 실제 호출 시 예외 발생 확인
        assertThatThrownBy(() -> userService.join("testGithubId", userDto))
                .isInstanceOf(UserExceptionHandler.class)
                .hasMessageContaining(UserErrorCode.ALREADY_JOINED_USER.getMessage());
    }

    @Test
    @DisplayName("updateInfo - 사용자 정보 수정 시 정상 반영되는지 테스트")
    void testUpdateInfo_Success() {
        // given
        given(userRepository.findByGithubId("testGithubId"))
                .willReturn(Optional.of(user));

        given(userRepository.save(any(User.class))).willReturn(user);

        Set<String> newStackNames = Set.of("FRONTEND");
        userDto = new UserDto(
                "newEmail@example.com",
                false,
                newStackNames,
                "https://example.com/newAvatar.png",
                "newUserName"
        );

        // mock: UserStack 삭제 후 저장
        given(userStackRepository.findAllByUserId(user.getId()))
                .willReturn(Set.of(new UserStack(user.getId(), Stack.BACKEND)));

        // when
        User updatedUser = userService.updateInfo("testGithubId", userDto);
        Set<UserStack> updatedStacks = userStackRepository.findAllByUserId(updatedUser.getId());

        // then
        assertThat(updatedUser.getEmail()).isEqualTo("newEmail@example.com");
        assertThat(updatedUser.getNotification()).isFalse();

        assertThat(updatedStacks).hasSize(1);
        assertThat(updatedStacks).extracting(UserStack::getStack)
                .map(Enum::name)
                .containsExactly("BACKEND");

        // repository 호출 검증
        verify(userRepository, times(1)).findByGithubId("testGithubId");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userStackRepository, times(1)).deleteAllByUserId(user.getId());
        verify(userStackRepository, times(1)).saveAll(anySet());
        verify(userStackRepository, times(1)).findAllByUserId(user.getId());
    }


}
