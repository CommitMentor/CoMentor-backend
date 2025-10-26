package com.knu.coment;

import com.knu.coment.config.auth.dto.OAuthAttributes;
import com.knu.coment.dto.UserDto;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserStack;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.UserException;
import com.knu.coment.exception.code.ProjectErrorCode;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStackRepository userStackRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User baseUser;
    private UserDto baseJoinDto;

    @BeforeEach
    void setUp() {
        baseUser = User.builder()
                .id(1L)
                .userName("tester")
                .email("initial@example.com")
                .notification(false)
                .userRole(Role.GUEST)
                .githubId("testGithubId")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        baseJoinDto = new UserDto(
                "joined@example.com",
                true,
                Set.of("FRONTEND", "BACKEND"),
                "https://example.com/avatar.png",
                "tester"
        );
    }

    @Test
    @DisplayName("findByGithubId - 등록된 깃허브 아이디면 User를 반환한다")
    void findByGithubId_returnsUser() {
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));

        User result = userService.findByGithubId(baseUser.getGithubId());

        assertThat(result).isSameAs(baseUser);
        verify(userRepository).findByGithubId(baseUser.getGithubId());
    }

    @Test
    @DisplayName("findByGithubId - 존재하지 않으면 UserException(NOT_FOUND_USER)을 던진다")
    void findByGithubId_missingUserThrows() {
        given(userRepository.findByGithubId("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByGithubId("missing"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.NOT_FOUND_USER.getMessage());

        verify(userRepository).findByGithubId("missing");
    }

    @Test
    @DisplayName("saveUser - 저장 요청을 그대로 UserRepository에 위임한다")
    void saveUser_delegatesToRepository() {
        given(userRepository.save(baseUser)).willReturn(baseUser);

        userService.saveUser(baseUser);

        verify(userRepository).save(baseUser);
    }

    @Test
    @DisplayName("saveOrUpdateGithub - 신규 계정이면 OAuth 정보로 엔티티를 생성해 저장한다")
    void saveOrUpdateGithub_createsNewUserWhenMissing() {
        OAuthAttributes attributes = OAuthAttributes.builder()
                .githubId("newGithubId")
                .name("New User")
                .avatarUrl("https://example.com/new.png")
                .attributes(Map.of(
                        "id", "newGithubId",
                        "name", "New User",
                        "avatar_url", "https://example.com/new.png"
                ))
                .nameAttributeKey("id")
                .build();

        given(userRepository.findByGithubId("newGithubId")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.saveOrUpdateGithub(attributes);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User persisted = captor.getValue();
        assertThat(persisted.getGithubId()).isEqualTo("newGithubId");
        assertThat(persisted.getUserRole()).isEqualTo(Role.GUEST);
        assertThat(persisted.getNotification()).isFalse();
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    @DisplayName("saveOrUpdateGithub - 기존 계정이면 조회된 엔티티를 재저장한다")
    void saveOrUpdateGithub_updatesExistingUser() {
        OAuthAttributes attributes = OAuthAttributes.builder()
                .githubId(baseUser.getGithubId())
                .name("Changed Name")
                .avatarUrl("https://example.com/changed.png")
                .attributes(Map.of(
                        "id", baseUser.getGithubId(),
                        "name", "Changed Name",
                        "avatar_url", "https://example.com/changed.png"
                ))
                .nameAttributeKey("id")
                .build();

        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(userRepository.save(baseUser)).willReturn(baseUser);

        User saved = userService.saveOrUpdateGithub(attributes);

        assertThat(saved).isSameAs(baseUser);
        verify(userRepository).save(baseUser);
    }

    @Test
    @DisplayName("join - 게스트 사용자가 이메일·알림·스택·기본 폴더와 함께 정회원으로 전환된다")
    void join_successfullyRegistersUser() {
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(userRepository.save(baseUser)).willReturn(baseUser);
        given(userStackRepository.saveAll(anySet())).willAnswer(invocation -> List.copyOf((Set<UserStack>) invocation.getArgument(0)));
        given(folderRepository.save(any(Folder.class))).willAnswer(invocation -> invocation.getArgument(0));

        User joined = userService.join(baseUser.getGithubId(), baseJoinDto);

        assertThat(joined.getUserRole()).isEqualTo(Role.USER);
        assertThat(joined.getEmail()).isEqualTo(baseJoinDto.getEmail());
        assertThat(joined.getNotification()).isEqualTo(baseJoinDto.isNotification());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<UserStack>> stackCaptor = ArgumentCaptor.forClass(Set.class);
        verify(userStackRepository).saveAll(stackCaptor.capture());
        Set<UserStack> persistedStacks = stackCaptor.getValue();
        assertThat(persistedStacks).hasSize(2);
        assertThat(persistedStacks.stream()
                .map(UserStack::getStack)
                .map(Enum::name))
                .containsExactlyInAnyOrderElementsOf(baseJoinDto.getStackNames());

        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(folderCaptor.capture());
        Folder defaultFolder = folderCaptor.getValue();
        assertThat(defaultFolder.getFileName()).isEqualTo("default");
        assertThat(defaultFolder.getUserId()).isEqualTo(baseUser.getId());

        verify(userRepository).save(baseUser);
    }

    @Test
    @DisplayName("join - 이미 USER 권한이면 ALREADY_JOINED_USER 예외를 던진다")
    void join_whenAlreadyUserThrows() {
        baseUser.updateRole(Role.USER);
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));

        assertThatThrownBy(() -> userService.join(baseUser.getGithubId(), baseJoinDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.ALREADY_JOINED_USER.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(userStackRepository, never()).saveAll(anySet());
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    @DisplayName("renewRefreshToken - 검증된 리프레시 토큰이면 새 토큰으로 교체한다")
    void renewRefreshToken_success() {
        baseUser.updateRefreshToken("oldToken");

        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(jwtTokenProvider.validateToken("oldToken")).willReturn(true);
        given(jwtTokenProvider.createRefreshToken(baseUser.getGithubId())).willReturn("newToken");
        given(userRepository.save(baseUser)).willReturn(baseUser);

        User refreshed = userService.renewRefreshToken(baseUser.getGithubId());

        assertThat(refreshed.getRefreshToken()).isEqualTo("newToken");
        verify(jwtTokenProvider).validateToken("oldToken");
        verify(jwtTokenProvider).createRefreshToken(baseUser.getGithubId());
        verify(userRepository).save(baseUser);
    }

    @Test
    @DisplayName("renewRefreshToken - 저장된 토큰이 없으면 MISSING_REQUIRED_FIELD 예외를 던진다")
    void renewRefreshToken_missingStoredTokenThrows() {
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));

        assertThatThrownBy(() -> userService.renewRefreshToken(baseUser.getGithubId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.MISSING_REQUIRED_FIELD.getMessage());

        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("renewRefreshToken - 저장된 토큰 검증에 실패하면 INVALID_REFRESH_TOKEN 예외를 던진다")
    void renewRefreshToken_invalidStoredTokenThrows() {
        baseUser.updateRefreshToken("expiredToken");
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(jwtTokenProvider.validateToken("expiredToken")).willReturn(false);

        assertThatThrownBy(() -> userService.renewRefreshToken(baseUser.getGithubId()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining(UserErrorCode.INVALID_REFRESH_TOKEN.getMessage());

        verify(jwtTokenProvider).validateToken("expiredToken");
        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserInfo - 사용자 정보와 스택 정보를 DTO로 변환해 반환한다")
    void getUserInfo_returnsDto() {
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        Set<UserStack> stacks = Set.of(
                new UserStack(baseUser.getId(), Stack.FRONTEND),
                new UserStack(baseUser.getId(), Stack.BACKEND)
        );
        given(userStackRepository.findAllByUserId(baseUser.getId())).willReturn(stacks);

        UserDto dto = userService.getUserInfo(baseUser.getGithubId());

        assertThat(dto.getEmail()).isEqualTo(baseUser.getEmail());
        assertThat(dto.isNotification()).isEqualTo(baseUser.getNotification());
        assertThat(dto.getStackNames()).containsExactlyInAnyOrder("FRONTEND", "BACKEND");
        assertThat(dto.getAvatarUrl()).isEqualTo(baseUser.getAvatarUrl());
        assertThat(dto.getUserName()).isEqualTo(baseUser.getUserName());
    }

    @Test
    @DisplayName("updateInfo - 기존 정보를 삭제하고 새 스택/이메일/알림 정보를 저장한다")
    void updateInfo_successfullyUpdatesUser() {
        UserDto updateDto = new UserDto(
                "updated@example.com",
                false,
                Set.of("BACKEND"),
                baseUser.getAvatarUrl(),
                baseUser.getUserName()
        );

        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(userRepository.save(baseUser)).willReturn(baseUser);
        given(userStackRepository.saveAll(anySet())).willAnswer(invocation -> List.copyOf((Set<UserStack>) invocation.getArgument(0)));

        User updated = userService.updateInfo(baseUser.getGithubId(), updateDto);

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getNotification()).isFalse();

        verify(userStackRepository).deleteAllByUserId(baseUser.getId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<UserStack>> stackCaptor = ArgumentCaptor.forClass(Set.class);
        verify(userStackRepository).saveAll(stackCaptor.capture());
        Set<UserStack> savedStacks = stackCaptor.getValue();
        assertThat(savedStacks).hasSize(1);
        assertThat(savedStacks.iterator().next().getStack()).isEqualTo(Stack.BACKEND);

        verify(userRepository).save(baseUser);
    }

    @Test
    @DisplayName("updateInfo - 스택이 비어 있으면 INVALID_RROJECT_STACK 예외를 던진다")
    void updateInfo_whenStacksEmptyThrows() {
        UserDto invalidDto = new UserDto(
                "updated@example.com",
                true,
                Set.of(),
                baseUser.getAvatarUrl(),
                baseUser.getUserName()
        );

        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));

        assertThatThrownBy(() -> userService.updateInfo(baseUser.getGithubId(), invalidDto))
                .isInstanceOf(ProjectException.class)
                .hasMessageContaining(ProjectErrorCode.INVALID_RROJECT_STACK.getMessage());

        verify(userStackRepository, never()).saveAll(anySet());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("withdrawn - 사용자 권한을 WITHDRAWN으로 업데이트한다")
    void withdrawn_updatesRole() {
        given(userRepository.findByGithubId(baseUser.getGithubId())).willReturn(Optional.of(baseUser));
        given(userRepository.save(baseUser)).willReturn(baseUser);

        User withdrawnUser = userService.withdrawn(baseUser.getGithubId());

        assertThat(withdrawnUser.getUserRole()).isEqualTo(Role.WITHDRAWN);
        verify(userRepository).save(baseUser);
    }
}
