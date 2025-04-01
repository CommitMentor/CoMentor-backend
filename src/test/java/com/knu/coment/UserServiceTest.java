package com.knu.coment;

import com.knu.coment.dto.UserDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.Role;
import com.knu.coment.repository.UserRepository;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // 설명: MockitoExtension을 사용하여 테스트 확장
class UserServiceTest {

    @Mock // 설명: Mock 객체 생성
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .userName("test")
                .email("test.@example.com")
                .notification(true)
                .userRole(Role.USER)
                .githubId("testGithubId")
                .avatarUrl("https://example.com/avatar.png")
                .build();
        Set<String> stackNames = new HashSet<>();
        stackNames.add("FRONTED");
        stackNames.add("BACKEND");
        userDto = new UserDto("test.@example.com", true, stackNames, "https://example.com/avatar.png", "test");

    }

    @Test
    @DisplayName("findByGithubId - 정상적으로 유저를 조회할 수 있어야 한다")
    void testFindByGithubId(){
        // given
        given(userRepository.findByGithubIdFetchStacks("testGithubId"))
                .willReturn(Optional.of(user));
        // when
        User result = userService.findByGithubId("testGithubId");
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGithubId()).isEqualTo("testGithubId");
        //userRepository 메서드가 1회 호출되었는지 검증
        verify(userRepository).findByGithubIdFetchStacks("testGithubId");
    }


}
