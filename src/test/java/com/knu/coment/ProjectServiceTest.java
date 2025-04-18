package com.knu.coment;

import com.knu.coment.dto.project_repo.*;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.Repo;
import com.knu.coment.entity.User;
import com.knu.coment.exception.ProjectException;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.global.Role;
import com.knu.coment.global.Status;
import com.knu.coment.repository.ProjectRepository;
import com.knu.coment.repository.RepoRepository;
import com.knu.coment.service.GithubRepoService;
import com.knu.coment.service.ProjectService;
import com.knu.coment.service.UserService;
import com.knu.coment.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private GithubRepoService githubRepoService;
    @Mock private UserService userService;
    @Mock private RepoRepository repoRepository;

    @InjectMocks private ProjectService projectService;

    private User user;
    private CreateProjectDto createProjectDto;
    private Project project;
    private RepoDto repoDto;
    private Repo repo;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .notification(true)
                .userRole(Role.GUEST)
                .githubId("testUser")
                .avatarUrl("https://example.com/avatar.png")
                .userName("test")
                .build();

        repo = new Repo(100L, "testRepo", "https://github.com/testUser/testRepo",
                "2021-01-01T00:00:00Z", "Java",
                new OwnerDto("testUser"));

        project = new Project("테스트 프로젝트", "백엔드", Status.PROGRESS, LocalDateTime.now(), user.getId(),repo.getId());

        createProjectDto = new CreateProjectDto(100L, "테스트 프로젝트", "백엔드", Status.PROGRESS);

        repoDto = new RepoDto(100L, "testRepo", "https://github.com/testUser/testRepo",  "Java", new OwnerDto("testUser"));
    }

    @Test
    @DisplayName("createProject: 정상 케이스")
    void createProject_Success() {
        List<RepoDto> repoDtos = new ArrayList<>();
        repoDtos.add(repoDto);

        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(githubRepoService.getUserRepos(user.getGithubAccessToken())).thenReturn(Mono.just(repoDtos));
        when(projectRepository.existsByUserIdAndRepoId(user.getId(), repoDto.getId())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project createdProject = projectService.createProject("testUser", createProjectDto);

        assertThat(createdProject.getDescription()).isEqualTo("테스트 프로젝트");
        assertThat(createdProject.getRole()).isEqualTo("백엔드");
        assertThat(createdProject.getStatus()).isEqualTo(Status.PROGRESS);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("createProject: 프로젝트 중복 예외")
    void createProject_DuplicateProjectThrowsException() {
        List<RepoDto> repoDtos = List.of(repoDto);
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(githubRepoService.getUserRepos(user.getGithubAccessToken())).thenReturn(Mono.just(repoDtos));
        when(projectRepository.existsByUserIdAndRepoId(user.getId(), repoDto.getId())).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject("testUser", createProjectDto))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.DUPLICATE_PROJECT.getMessage());
    }

    @Test
    @DisplayName("updateProject: 정상 케이스")
    void updateProject_Success() {
        UpdateRepoDto updateDto = new UpdateRepoDto("수정된 설명", "프론트엔드", Status.DONE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project updated = projectService.updateProject("testUser", 1L, updateDto);

        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getRole()).isEqualTo("프론트엔드");
        assertThat(updated.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    @DisplayName("updateProject: 권한 예외")
    void updateProject_UnauthorizedUser() {
        // given
        UpdateRepoDto updateDto = new UpdateRepoDto("설명", "역할", Status.DONE);
        User otherUser = User.builder()
                .id(999L) // ❗프로젝트의 userId와 다른 값
                .githubId("wrongUser")
                .email("wrong@example.com")
                .notification(true)
                .userRole(Role.USER)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.findByGithubId("wrongUser")).thenReturn(otherUser); // null 대신 유저 객체

        // when & then
        assertThatThrownBy(() -> projectService.updateProject("wrongUser", 1L, updateDto))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }


    @Test
    @DisplayName("getUserProjects: 정상 조회")
    void getUserProjects_Success() {
        when(userService.findByGithubId("testUser")).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 8, Sort.by("id").descending());

        List<Project> projectList = List.of(project);
        Page<Project> page = new PageImpl<>(projectList, pageable, projectList.size());

        when(projectRepository.findAllByUserId(user.getId(), pageable)).thenReturn(page);
        when(repoRepository.findById(project.getRepoId())).thenReturn(Optional.of(repo));

        PageResponse<DashBoardDto> response = projectService.getUserProjects("testUser", null, 0);

        assertThat(response.getContent()).hasSize(1);
        DashBoardDto dto = response.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(project.getId());
        assertThat(dto.getName()).isEqualTo(repo.getName());
    }

    @Test
    @DisplayName("getProjectInfo: 정상 조회")
    void getProjectInfo_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(repoRepository.findById(project.getRepoId())).thenReturn(Optional.of(repo));
        when(userService.findByGithubId("testUser")).thenReturn(user);

        DashBoardDto result = projectService.getProjectInfo("testUser", 1L);

        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getDescription()).isEqualTo(project.getDescription());
    }

    @Test
    @DisplayName("getProjectInfo: 권한 예외")
    void getProjectInfo_Unauthorized() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // 권한 없는 다른 유저를 mock으로 등록
        User wrongUser = User.builder()
                .id(999L)
                .githubId("wrongUser")
                .email("wrong@example.com")
                .notification(true)
                .userRole(Role.USER)
                .build();
        when(userService.findByGithubId("wrongUser")).thenReturn(wrongUser);

        assertThatThrownBy(() -> projectService.getProjectInfo("wrongUser", 1L))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }


    @Test
    @DisplayName("deleteProject: 미존재 프로젝트")
    void deleteProject_NotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject("testUser", 999L))
                .isInstanceOf(ProjectException.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }
}