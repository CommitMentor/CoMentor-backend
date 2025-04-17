package com.knu.coment.service;

import com.knu.coment.dto.project_repo.*;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.ProjectCsQuestion;
import com.knu.coment.entity.Repo;
import com.knu.coment.entity.User;
import com.knu.coment.exception.ProjectExceptionHandler;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.global.Status;
import com.knu.coment.repository.AnswerRepository;
import com.knu.coment.repository.ProjectCsQuestionRepository;
import com.knu.coment.repository.ProjectRepository;
import com.knu.coment.repository.RepoRepository;
import com.knu.coment.util.PageResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final GithubRepoService githubRepoService;
    private final UserService userService;
    private final AnswerRepository answerRepository;
    private final CsQuestionService cs;
    private final ProjectCsQuestionRepository projectCsQuestionRepository;
    private final RepoRepository repoRepository;

    public Project findById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_PROJECT));
    }
    public void isProjectOwner(String githubId, Long projectId) {
        Project project = findById(projectId);
        User user = userService.findByGithubId(githubId);
        if (!project.getUserId().equals(user.getId())) {
            throw new ProjectExceptionHandler(ProjectErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
    @Transactional
    public Project createProject(String githubId, CreateProjectDto dto) {
        User user = userService.findByGithubId(githubId);
        String accessToken = user.getGithubAccessToken();
        List<RepoDto> repoList = githubRepoService.getUserRepos(accessToken).block();
        RepoDto repodto = repoList.stream()
                .filter(r -> r.getId().equals(dto.getId()))
                .findFirst()
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_PROJECT));
        repoRepository.save(repodto.toEntity());
        boolean projectExists = projectRepository.existsByUserIdAndRepoId(user.getId(), repodto.getId());
        if (projectExists) {
            throw new ProjectExceptionHandler(ProjectErrorCode.DUPLICATE_PROJECT);
        }
        Project project = new Project(
                dto.getDescription(),
                dto.getRole(),
                dto.getStatus(),
                LocalDateTime.now(),
                user.getId(),
                dto.getId()
        );
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(String githubId, Long projectId, UpdateRepoDto dto) {
        Project project = findById(projectId);
        isProjectOwner(githubId, projectId);
        project.update(dto.getDescription(), dto.getRole(), dto.getStatus());
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<DashBoardDto> getUserProjects(String githubId, Status status, int page) {
        User user = userService.findByGithubId(githubId);
        Pageable pageable = PageRequest.of(page, 8, Sort.by("id").descending());

        Page<Project> projectsPage = (status != null)
                ? projectRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : projectRepository.findAllByUserId(user.getId(), pageable);

        List<DashBoardDto> dtoList = projectsPage.getContent().stream()
                .map(project -> {
                    Repo repo = null;
                    if (project.getRepoId() != null) {
                        repo = repoRepository.findById(project.getRepoId()).orElse(null);
                    }
                    return DashBoardDto.fromEntity(project, repo);
                })
                .toList();

        return new PageResponse<>(new PageImpl<>(dtoList, pageable, projectsPage.getTotalElements()));
    }


    public DashBoardDto getProjectInfo(String githubId, Long projectId){
        Project project = findById(projectId);
        isProjectOwner(githubId, projectId);
        Repo repo = repoRepository.findById(project.getRepoId())
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_Repo));
        return DashBoardDto.fromEntity(project, repo);
    }
    public void deleteProject(String githubId, Long projectId) {
        userService.findByGithubId(githubId);
        findById(projectId);
        isProjectOwner(githubId, projectId);

        List<ProjectCsQuestion> questionLinks = projectCsQuestionRepository.findAllByProjectId(projectId);

        for (ProjectCsQuestion link : questionLinks) {
            cs.deleteProjectCsQuestion(link.getId());
        }

        projectRepository.deleteById(projectId);
    }

}
