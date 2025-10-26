package com.knu.coment;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.FolderListDto;
import com.knu.coment.dto.project_repo.OwnerDto;
import com.knu.coment.entity.Folder;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.Question;
import com.knu.coment.entity.Repo;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserCSQuestion;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.exception.code.QuestionErrorCode;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import com.knu.coment.global.Status;
import com.knu.coment.repository.FolderRepository;
import com.knu.coment.repository.ProjectRepository;
import com.knu.coment.repository.QuestionRepository;
import com.knu.coment.repository.RepoRepository;
import com.knu.coment.repository.UserCSQuestionRepository;
import com.knu.coment.service.FolderService;
import com.knu.coment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private RepoRepository repoRepository;

    @Mock
    private UserCSQuestionRepository userCSQuestionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private FolderService folderService;

    private User user;
    private Folder baseFolder;
    private Project project;
    private Repo repo;
    private Question projectQuestion;
    private Question csQuestion;
    private UserCSQuestion userCSQuestion;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .userName("tester")
                .githubId("github")
                .email("tester@example.com")
                .notification(true)
                .build();

        baseFolder = new Folder("base", user.getId());
        baseFolder.setId(10L);

        repo = new Repo(100L, "repo", "https://example.com/repo", "2020-01-01T00:00:00Z", "Java", new OwnerDto("tester"));
        project = new Project("desc", "role", Status.PROGRESS, LocalDateTime.now(), user.getId(), repo.getId());
        ReflectionTestUtils.setField(project, "id", 200L);

        projectQuestion = new Question(
                CSCategory.DATABASES,
                QuestionType.PROJECT,
                "code",
                "question",
                LocalDateTime.now(),
                QuestionStatus.TODO,
                baseFolder.getFileName(),
                null,
                user.getId(),
                project.getId()
        );
        ReflectionTestUtils.setField(projectQuestion, "id", 301L);

        csQuestion = new Question(
                CSCategory.NETWORKING,
                QuestionType.CS,
                null,
                "cs question",
                LocalDateTime.now(),
                QuestionStatus.TODO,
                null,
                null,
                user.getId(),
                null
        );
        ReflectionTestUtils.setField(csQuestion, "id", 302L);

        userCSQuestion = new UserCSQuestion(user.getId(), csQuestion.getId(), LocalDate.now(), QuestionStatus.TODO);
        userCSQuestion.bookMark(baseFolder.getFileName());
        ReflectionTestUtils.setField(userCSQuestion, "id", 401L);
    }

    @Test
    @DisplayName("getFolderList - 사용자 폴더가 id 오름차순으로 정렬되어 반환된다")
    void getFolderList_sortsByIdAscending() {
        Folder newer = new Folder("z", user.getId());
        newer.setId(30L);
        Folder older = new Folder("a", user.getId());
        older.setId(20L);
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findAllByUserId(user.getId())).willReturn(List.of(newer, older));

        List<FolderListDto> result = folderService.getFolderList(user.getGithubId());

        assertThat(result).extracting(FolderListDto::getFolderId).containsExactly(20L, 30L);
        verify(folderRepository).findAllByUserId(user.getId());
    }

    @Test
    @DisplayName("getFolderQuestions - 프로젝트/CS 질문을 모두 포함한 목록을 반환한다")
    void getFolderQuestions_returnsProjectAndCsEntries() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(projectQuestion));
        given(projectRepository.findById(projectQuestion.getProjectId())).willReturn(Optional.of(project));
        given(repoRepository.findById(project.getRepoId())).willReturn(Optional.of(repo));
        given(userCSQuestionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(userCSQuestion));
        given(questionRepository.findAllById(List.of(userCSQuestion.getQuestionId()))).willReturn(List.of(csQuestion));

        List<FolderCsQuestionListDto> result = folderService.getFolderQuestions(user.getGithubId(), baseFolder.getId());

        assertThat(result).hasSize(2);
        FolderCsQuestionListDto projectEntry = result.get(0);
        assertThat(projectEntry.getQuestionId()).isEqualTo(projectQuestion.getId());
        assertThat(projectEntry.getRepoName()).isEqualTo(repo.getName());
        FolderCsQuestionListDto csEntry = result.get(1);
        assertThat(csEntry.getCsQuestionId()).isEqualTo(userCSQuestion.getId());
        assertThat(csEntry.getCsCategory()).isEqualTo(csQuestion.getCsCategory());
    }

    @Test
    @DisplayName("getFolderQuestions - 프로젝트를 찾을 수 없으면 예외를 던진다")
    void getFolderQuestions_missingProjectThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(projectQuestion));
        given(projectRepository.findById(projectQuestion.getProjectId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.getFolderQuestions(user.getGithubId(), baseFolder.getId()))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }

    @Test
    @DisplayName("getFolderQuestions - 저장소를 찾을 수 없으면 예외를 던진다")
    void getFolderQuestions_missingRepoThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(projectQuestion));
        given(projectRepository.findById(projectQuestion.getProjectId())).willReturn(Optional.of(project));
        given(repoRepository.findById(project.getRepoId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.getFolderQuestions(user.getGithubId(), baseFolder.getId()))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_REPO.getMessage());
    }

    @Test
    @DisplayName("getFolderQuestions - CS 원본 질문이 없으면 예외를 던진다")
    void getFolderQuestions_missingCsQuestionThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of());
        given(userCSQuestionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(userCSQuestion));
        given(questionRepository.findAllById(List.of(userCSQuestion.getQuestionId()))).willReturn(List.of());

        assertThatThrownBy(() -> folderService.getFolderQuestions(user.getGithubId(), baseFolder.getId()))
                .isInstanceOf(FolderException.class)
                .hasMessage(QuestionErrorCode.NOT_FOUND_QUESTION.getMessage());
    }

    @Test
    @DisplayName("bookmarkQuestion - 폴더가 없으면 새로 생성하고 프로젝트 질문을 북마크한다")
    void bookmarkQuestion_createsFolderForProjectQuestion() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setQuestionId(projectQuestion.getId());
        dto.setFileName("  New Folder ");

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), "NewFolder")).willReturn(Optional.empty());
        given(folderRepository.save(any(Folder.class))).willAnswer(invocation -> {
            Folder saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        given(questionRepository.findByIdAndUserId(projectQuestion.getId(), user.getId())).willReturn(Optional.of(projectQuestion));

        folderService.bookmarkQuestion(user.getGithubId(), dto);

        assertThat(projectQuestion.getFileName()).isEqualTo("NewFolder");
        ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
        verify(folderRepository).save(folderCaptor.capture());
        assertThat(folderCaptor.getValue().getFileName()).isEqualTo("NewFolder");
        verify(questionRepository).save(projectQuestion);
    }

    @Test
    @DisplayName("bookmarkQuestion - 폴더명을 입력하지 않으면 기본 이름으로 생성한다")
    void bookmarkQuestion_usesGeneratedDefaultNameWhenMissing() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setQuestionId(projectQuestion.getId());
        dto.setFileName(null);

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.countByUserIdAndFileNameStartingWith(user.getId(), "폴더")).willReturn(0L);
        given(folderRepository.findByUserIdAndFileName(user.getId(), "폴더")).willReturn(Optional.empty());
        given(folderRepository.save(any(Folder.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(questionRepository.findByIdAndUserId(projectQuestion.getId(), user.getId())).willReturn(Optional.of(projectQuestion));

        folderService.bookmarkQuestion(user.getGithubId(), dto);

        assertThat(projectQuestion.getFileName()).isEqualTo("폴더");
        verify(folderRepository).save(argThat(folder -> "폴더".equals(folder.getFileName())));
    }

    @Test
    @DisplayName("bookmarkQuestion - CS 질문 북마크 시 사용자 질문이 업데이트된다")
    void bookmarkQuestion_updatesUserCsQuestion() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setCsQuestionId(userCSQuestion.getId());
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));
        given(userCSQuestionRepository.findByIdAndUserId(userCSQuestion.getId(), user.getId())).willReturn(Optional.of(userCSQuestion));

        folderService.bookmarkQuestion(user.getGithubId(), dto);

        verify(userCSQuestionRepository).save(userCSQuestion);
        assertThat(userCSQuestion.getFileName()).isEqualTo(baseFolder.getFileName());
    }

    @Test
    @DisplayName("bookmarkQuestion - 질문 ID가 없으면 예외를 던진다")
    void bookmarkQuestion_missingIdentifiersThrows() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setFileName("name");

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);

        assertThatThrownBy(() -> folderService.bookmarkQuestion(user.getGithubId(), dto))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.MISSING_REQUIRED_FIELD.getMessage());
    }

    @Test
    @DisplayName("cancelBookmark - 프로젝트 질문 북마크를 해제한다")
    void cancelBookmark_clearsProjectBookmark() {
        projectQuestion.bookMark(baseFolder.getFileName());
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setQuestionId(projectQuestion.getId());
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findByIdAndUserId(projectQuestion.getId(), user.getId())).willReturn(Optional.of(projectQuestion));

        folderService.cancelBookmark(user.getGithubId(), dto);

        assertThat(projectQuestion.getFileName()).isNull();
        verify(questionRepository).save(projectQuestion);
    }

    @Test
    @DisplayName("cancelBookmark - 폴더명이 일치하지 않으면 BAD_REQUEST 예외")
    void cancelBookmark_folderMismatchThrows() {
        projectQuestion.bookMark("other");
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setQuestionId(projectQuestion.getId());
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findByIdAndUserId(projectQuestion.getId(), user.getId())).willReturn(Optional.of(projectQuestion));

        assertThatThrownBy(() -> folderService.cancelBookmark(user.getGithubId(), dto))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("cancelBookmark - CS 질문 북마크 해제")
    void cancelBookmark_clearsUserCsBookmark() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setCsQuestionId(userCSQuestion.getId());
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));
        given(userCSQuestionRepository.findByIdAndUserId(userCSQuestion.getId(), user.getId())).willReturn(Optional.of(userCSQuestion));

        folderService.cancelBookmark(user.getGithubId(), dto);

        assertThat(userCSQuestion.getFileName()).isNull();
        verify(userCSQuestionRepository).save(userCSQuestion);
    }

    @Test
    @DisplayName("cancelBookmark - CS 질문 폴더가 다른 경우 BAD_REQUEST")
    void cancelBookmark_userCsFolderMismatchThrows() {
        userCSQuestion.bookMark("different");
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setCsQuestionId(userCSQuestion.getId());
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));
        given(userCSQuestionRepository.findByIdAndUserId(userCSQuestion.getId(), user.getId())).willReturn(Optional.of(userCSQuestion));

        assertThatThrownBy(() -> folderService.cancelBookmark(user.getGithubId(), dto))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.BAD_REQUEST.getMessage());
    }

    @Test
    @DisplayName("cancelBookmark - 식별자가 없으면 예외")
    void cancelBookmark_missingIdentifiersThrows() {
        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setFileName(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(Optional.of(baseFolder));

        assertThatThrownBy(() -> folderService.cancelBookmark(user.getGithubId(), dto))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.MISSING_REQUIRED_FIELD.getMessage());
    }

    @Test
    @DisplayName("updateFolderName - 새 이름이 정상 저장된다")
    void updateFolderName_updatesCleanName() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(folderRepository.existsByUserIdAndFileName(user.getId(), "NewName")).willReturn(false);

        folderService.updateFolderName(user.getGithubId(), baseFolder.getId(), " New Name ");

        assertThat(baseFolder.getFileName()).isEqualTo("NewName");
        verify(folderRepository).save(baseFolder);
    }

    @Test
    @DisplayName("updateFolderName - 중복되면 예외")
    void updateFolderName_duplicateThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(folderRepository.existsByUserIdAndFileName(user.getId(), "Dup")).willReturn(true);

        assertThatThrownBy(() -> folderService.updateFolderName(user.getGithubId(), baseFolder.getId(), "Dup"))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.DUPLICATE_FILE_NAME.getMessage());
    }

    @Test
    @DisplayName("updateFolderName - 비어있는 이름이면 예외")
    void updateFolderName_blankThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));

        assertThatThrownBy(() -> folderService.updateFolderName(user.getGithubId(), baseFolder.getId(), "   "))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.MISSING_REQUIRED_FIELD.getMessage());
    }

    @Test
    @DisplayName("deleteFolder - 포함된 모든 질문 북마크를 해제하고 폴더를 삭제한다")
    void deleteFolder_unbookmarksQuestionsAndDeletesFolder() {
        projectQuestion.bookMark(baseFolder.getFileName());
        userCSQuestion.bookMark(baseFolder.getFileName());

        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.of(baseFolder));
        given(questionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(projectQuestion));
        given(userCSQuestionRepository.findAllByUserIdAndFileName(user.getId(), baseFolder.getFileName())).willReturn(List.of(userCSQuestion));

        folderService.deleteFolder(user.getGithubId(), baseFolder.getId());

        assertThat(projectQuestion.getFileName()).isNull();
        assertThat(userCSQuestion.getFileName()).isNull();
        verify(questionRepository).save(projectQuestion);
        verify(userCSQuestionRepository).save(userCSQuestion);
        verify(folderRepository).delete(baseFolder);
    }

    @Test
    @DisplayName("deleteFolder - 폴더가 없으면 예외")
    void deleteFolder_missingFolderThrows() {
        given(userService.findByGithubId(user.getGithubId())).willReturn(user);
        given(folderRepository.findByUserIdAndId(user.getId(), baseFolder.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.deleteFolder(user.getGithubId(), baseFolder.getId()))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.NOT_FOUND_FOLDER.getMessage());
    }
}
