package com.knu.coment;

import com.knu.coment.dto.BookMarkRequestDto;
import com.knu.coment.dto.FolderCsQuestionListDto;
import com.knu.coment.dto.project_repo.OwnerDto;
import com.knu.coment.entity.*;
import com.knu.coment.exception.FolderException;
import com.knu.coment.exception.code.FolderErrorCode;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import com.knu.coment.global.Status;
import com.knu.coment.repository.*;
import com.knu.coment.service.FolderService;
import com.knu.coment.service.ProjectQuestionService;
import com.knu.coment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class FolderServiceTest {

    @Mock private UserService userService;
    @Mock private QuestionRepository questionRepository;
    @Mock private ProjectQuestionService projectQuestionService;
    @Mock private FolderRepository folderRepository;
    @Mock private RepoRepository repoRepository;
    @Mock private UserCSQuestionRepository userCSQuestionRepository;
    @Mock private ProjectRepository projectRepository;

    @InjectMocks private FolderService folderService;

    private User user;
    private Folder folder;
    private Question question;
    private UserCSQuestion userCSQuestion;
    private Project project;
    private Repo repo;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .userName("testUser")
                .githubId("testGithub")
                .build();

        folder = new Folder("default", user.getId());
        folder.setId(10L);

        question = new Question(CSCategory.DATABASES, QuestionType.PROJECT, "ddd",
                "ddd",LocalDateTime.now(), QuestionStatus.TODO, "default", "default", 1L,1L);
        ReflectionTestUtils.setField(question, "id", 1L);
        question.bookMark("default");

        repo = new Repo(100L, "testRepo", "https://github.com/testUser/testRepo",
                "2021-01-01T00:00:00Z", "Java",
                new OwnerDto("testUser"));

        project = new Project("테스트 프로젝트", "백엔드", Status.PROGRESS, LocalDateTime.now(), user.getId(),repo.getId());


        userCSQuestion = new UserCSQuestion(user.getId(), 1L, LocalDate.now(), QuestionStatus.TODO);
        userCSQuestion.bookMark("default");
    }

    @Test
    @DisplayName("getFolderQuestions - 성공 (ProjectQuestion + UserCSQuestion 모두 존재)")
    void getFolderQuestions_Success() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(folder));
        when(questionRepository.findAllByFileName(anyString())).thenReturn(List.of(question));
        when(userCSQuestionRepository.findAllByUserIdAndFileName(anyLong(), anyString())).thenReturn(List.of(userCSQuestion));
        when(questionRepository.findAllById(anyList())).thenReturn(List.of(question));

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        when(repoRepository.findById(anyLong())).thenReturn(Optional.of(repo));

        List<FolderCsQuestionListDto> result = folderService.getFolderQuestions("testGithub", 10L);

        assertThat(result).isNotEmpty();
    }


    @Test
    @DisplayName("getFolderQuestions - 폴더 없음 예외")
    void getFolderQuestions_FolderNotFound() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.getFolderQuestions("testGithub", 10L))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.NOT_FOUND_FOLDER.getMessage());
    }

    @Test
    @DisplayName("bookmarkQuestion - 프로젝트 질문 북마크")
    void bookmarkQuestion_ProjectQuestion() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndFileName(anyLong(), anyString()))
                .thenReturn(Optional.of(folder));
        when(projectQuestionService.findById(anyLong())).thenReturn(question);
        when(questionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(question));


        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setQuestionId(1L);
        dto.setFileName("default");

        folderService.bookmarkQuestion("testGithub", dto);

        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("bookmarkQuestion - CS 질문 북마크")
    void bookmarkQuestion_UserCSQuestion() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndFileName(anyLong(), anyString()))
                .thenReturn(Optional.of(folder));
        when(userCSQuestionRepository.findById(anyLong())).thenReturn(Optional.of(userCSQuestion));
        when(userCSQuestionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(userCSQuestion));



        BookMarkRequestDto dto = new BookMarkRequestDto();
        dto.setCsQuestionId(1L);
        dto.setFileName("default");

        folderService.bookmarkQuestion("testGithub", dto);

        verify(userCSQuestionRepository).save(any(UserCSQuestion.class));
    }

    @Test
    @DisplayName("deleteFolder - 정상 폴더 삭제")
    void deleteFolder_Success() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(folder));
        when(questionRepository.findAllByFileName(anyString())).thenReturn(List.of(question));
        when(userCSQuestionRepository.findAllByUserIdAndFileName(anyLong(), anyString())).thenReturn(List.of(userCSQuestion));

        folderService.deleteFolder("testGithub", 10L);

        verify(folderRepository).delete(any(Folder.class));
    }

    @Test
    @DisplayName("deleteFolder - 폴더 없음 예외")
    void deleteFolder_FolderNotFound() {
        when(userService.findByGithubId(anyString())).thenReturn(user);
        when(folderRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.deleteFolder("testGithub", 10L))
                .isInstanceOf(FolderException.class)
                .hasMessage(FolderErrorCode.NOT_FOUND_FOLDER.getMessage());
    }
}
