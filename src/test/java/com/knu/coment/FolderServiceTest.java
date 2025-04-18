package com.knu.coment;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.User;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.global.Role;
import com.knu.coment.repository.FolderRepository;
import com.knu.coment.repository.ProjectCsQuestionRepository;
import com.knu.coment.service.CsQuestionService;
import com.knu.coment.service.FolderService;
import com.knu.coment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {

    @Mock private UserService userService;
    @Mock private ProjectCsQuestionRepository projectCsQuestionRepository;
    @Mock private CsQuestionService csQuestionService;
    @Mock private FolderRepository folderRepository;

    @InjectMocks private FolderService folderService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .githubId("testGithubId")
                .email("test@example.com")
                .userRole(Role.USER)
                .notification(true)
                .userName("Tester")
                .avatarUrl("https://avatar")
                .build();
    }

    @Test
    @DisplayName("getFolderList - 폴더 목록 정상 조회")
    void getFolderList_success() {
        Folder folder = new Folder("CS", user.getId());
        folder.setId(1L);

        when(userService.findByGithubId("testGithubId")).thenReturn(user);
        when(folderRepository.findAllByUserId(user.getId())).thenReturn(List.of(folder));

        List<FolderListDto> folders = folderService.getFolderList("testGithubId");

        assertThat(folders).hasSize(1);
        assertThat(folders.get(0).getFileName()).isEqualTo("CS");
    }

    @Test
    @DisplayName("bookmarkQuestion - 필수값 누락 예외")
    void bookmarkQuestion_missingFolderName() {
        BookMarkRequestDto dto = new BookMarkRequestDto("   ", 1L);

        when(userService.findByGithubId("testGithubId")).thenReturn(user);

        assertThatThrownBy(() -> folderService.bookmarkQuestion("testGithubId", dto))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.MISSING_REQUIRED_FIELD.getMessage());
    }

    @Test
    @DisplayName("deleteFolder - 폴더 없음 예외")
    void deleteFolder_notFound() {
        when(userService.findByGithubId("testGithubId")).thenReturn(user);
        when(folderRepository.findByUserIdAndId(user.getId(), 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.deleteFolder("testGithubId", 999L))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.NOT_FOUND_FOLDER.getMessage());
    }
}
