package com.knu.coment.service;

import com.knu.coment.dto.project_repo.*;
import com.knu.coment.entity.Project;
import com.knu.coment.entity.Repo;
import com.knu.coment.entity.User;
import com.knu.coment.exception.ProjectExceptionHandler;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.repository.ProjectRepository;
import com.knu.coment.repository.RepoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final GithubRepoService githubRepoService;
    private final UserService userService;
    private final RepoRepository repoRepository;

    @Transactional
    public Project createProject(String githubId, CreateProjectDto dto) {
        User user = userService.findByGithubId(githubId);
        String accessToken = user.getGithubAccessToken();
        List<RepoDto> repoList = githubRepoService.getUserRepos(accessToken).block();
        RepoDto repodto = repoList.stream()
                .filter(r -> r.getId().equals(dto.getId()))
                .findFirst()
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_PROJECT));
        boolean projectExists = projectRepository.existsByUserAndRepoId(user, repodto.getId());
        if (projectExists) {
            throw new ProjectExceptionHandler(ProjectErrorCode.DUPLICATE_PROJECT);
        }
        Project project = dto.toEntity();
        project.assignUser(user);
        Repo repo = repoRepository.findById(repodto.getId())
                .orElseGet(() -> repoRepository.save(repodto.toEntity()));
        project.assignRepo(repo);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(String githubId, Long projectId, UpdateRepoDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_PROJECT));
        User projectOwner = project.getUser();
        if (!projectOwner.getGithubId().equals(githubId)) {
            throw new ProjectExceptionHandler(ProjectErrorCode.UNAUTHORIZED_ACCESS);
        }
        project.update(dto.getDescription(), dto.getRole(), dto.getStatus());
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<DashBoardDto> getUserProjects(String githubId) {
        User user = userService.findByGithubId(githubId);
        List<Project> projects = projectRepository.findAllByUser(user);

        return projects.stream()
                .map(project -> {
                    Repo repo = project.getRepo();
                    return new DashBoardDto(
                            project.getId(),
                            (repo != null) ? repo.getName() : null,
                            (repo != null) ? repo.getLanguage() : null,
                            project.getDescription(),
                            project.getRole(),
                            project.getStatus(),
                            (repo != null) ? repo.getUpdatedAt() : null,
                            repo.getOwner().getLogin()
                    );
                })
                .sorted(Comparator.comparing(DashBoardDto::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }
    public void deleteProject(String githubId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_PROJECT));
        User projectOwner = project.getUser();
        if (!projectOwner.getGithubId().equals(githubId)) {
            throw new ProjectExceptionHandler(ProjectErrorCode.UNAUTHORIZED_ACCESS);
        }
        projectRepository.delete(project);
    }
}
