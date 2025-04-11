package com.knu.coment;

import com.knu.coment.dto.project_repo.*;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.Repo;
import com.knu.coment.entity.User;
import com.knu.coment.exception.ProjectExceptionHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private GithubRepoService githubRepoService;
    @Mock
    private UserService userService;
    @Mock
    private RepoRepository repoRepository;

    @InjectMocks
    private ProjectService projectService;

    private User user;
    private CreateProjectDto createProjectDto;
    private Project project;
    private RepoDto repoDto;
    private Repo repo;

    @BeforeEach
    void setUp() {
        // User 엔티티의 builder 사용
        user = User.builder()
                .email("test@example.com")
                .notification(true)
                .userRole(Role.GUEST)
                .githubId("testUser")
                //.githubAccessToken("testAccessToken")
                .avatarUrl("https://example.com/avatar.png")
                .userName("test")
                .build();

        repo = new Repo(
                100L,
                "testRepo",
                "https://github.com/testUser/testRepo",
                "2021-01-01T00:00:00Z",
                "2021-01-02T00:00:00Z",
                "Java",
                new OwnerDto("testUser")
        );

        project = new Project(
                "테스트 프로젝트",
                "백엔드",
                Status.PROGRESS
        );
        project.assignUser(user);
        project.assignRepo(repo);

        createProjectDto = new CreateProjectDto(
                100L,
                "테스트 프로젝트",
                "백엔드",
                Status.PROGRESS
        );

        repoDto = new RepoDto(
                100L,
                "testRepo",
                "https://github.com/testUser/testRepo",
                "2021-01-01T00:00:00Z",
                "2021-01-02T00:00:00Z",
                "Java",
                new OwnerDto("testUser")
        );
    }

    @Test
    @DisplayName("createProject: 정상 케이스")
    void createProject_Success() {
        // given
        List<RepoDto> repoDtos = new ArrayList<>();
        repoDtos.add(repoDto);

        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(githubRepoService.getUserRepos(user.getGithubAccessToken())).thenReturn(Mono.just(repoDtos));
        when(projectRepository.existsByUserAndRepoId(user, repoDto.getId())).thenReturn(false);
        when(repoRepository.findById(repoDto.getId())).thenReturn(Optional.empty());
        when(repoRepository.save(any(Repo.class))).thenReturn(repo);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // when
        Project createdProject = projectService.createProject("testUser", createProjectDto);

        // then
        assertThat(createdProject.getDescription()).isEqualTo("테스트 프로젝트");
        assertThat(createdProject.getRole()).isEqualTo("백엔드");
        assertThat(createdProject.getStatus()).isEqualTo(Status.PROGRESS);
        verify(projectRepository).save(any(Project.class));
        verify(repoRepository).save(any(Repo.class));
    }

    @Test
    @DisplayName("createProject: 프로젝트 중복 예외")
    void createProject_DuplicateProjectThrowsException() {
        // given
        List<RepoDto> repoDtos = new ArrayList<>();
        repoDtos.add(repoDto);

        when(userService.findByGithubId("testUser")).thenReturn(user);
        when(githubRepoService.getUserRepos(user.getGithubAccessToken())).thenReturn(Mono.just(repoDtos));
        when(projectRepository.existsByUserAndRepoId(user, repoDto.getId())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> projectService.createProject("testUser", createProjectDto))
                .isInstanceOf(ProjectExceptionHandler.class)
                .hasMessage(ProjectErrorCode.DUPLICATE_PROJECT.getMessage());
    }

    @Test
    @DisplayName("updateProject: 정상 케이스")
    void updateProject_Success() {
        // given
        UpdateRepoDto updateDto = new UpdateRepoDto("수정된 설명", "프론트엔드", Status.DONE);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // when
        Project updated = projectService.updateProject("testUser", 1L, updateDto);

        // then
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getRole()).isEqualTo("프론트엔드");
        assertThat(updated.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    @DisplayName("updateProject: 권한 예외")
    void updateProject_UnauthorizedUser() {
        // given
        UpdateRepoDto updateDto = new UpdateRepoDto("설명", "역할", Status.DONE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> projectService.updateProject("wrongUser", 1L, updateDto))
                .isInstanceOf(ProjectExceptionHandler.class)
                .hasMessage(ProjectErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("getUserProjects: 정상 조회")
    void getUserProjects_Success() {
        // given
        when(userService.findByGithubId("testUser")).thenReturn(user);
        Pageable pageable = PageRequest.of(0, 8, Sort.by("repo.updatedAt").descending());

        List<Project> projectList = List.of(project);
        Page<Project> page = new PageImpl<>(projectList, pageable, projectList.size());

        when(projectRepository.findAllByUser(user, pageable)).thenReturn(page);

        // when
        PageResponse<DashBoardDto> response = projectService.getUserProjects("testUser", null, 0);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(project.getId());
    }

    @Test
    @DisplayName("getProjectInfo: 정상 조회")
    void getProjectInfo_Success() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when
        DashBoardDto result = projectService.getProjectInfo("testUser", 1L);

        // then
        assertThat(result.getId()).isEqualTo(project.getId());
        assertThat(result.getDescription()).isEqualTo(project.getDescription());
    }

    @Test
    @DisplayName("getProjectInfo: 권한 예외")
    void getProjectInfo_Unauthorized() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> projectService.getProjectInfo("wrongUser", 1L))
                .isInstanceOf(ProjectExceptionHandler.class)
                .hasMessage(ProjectErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("deleteProject: 정상 삭제")
    void deleteProject_Success() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when
        projectService.deleteProject("testUser", 1L);

        // then
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    @DisplayName("deleteProject: 권한 예외")
    void deleteProject_Unauthorized() {
        // given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject("wrongUser", 1L))
                .isInstanceOf(ProjectExceptionHandler.class)
                .hasMessage(ProjectErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("deleteProject: 미존재 프로젝트")
    void deleteProject_NotFound() {
        // given
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject("testUser", 999L))
                .isInstanceOf(ProjectExceptionHandler.class)
                .hasMessage(ProjectErrorCode.NOT_FOUND_PROJECT.getMessage());
    }
}
