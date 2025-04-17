package com.knu.coment.controller;

import com.knu.coment.dto.project_repo.RepoDto;
import com.knu.coment.dto.project_repo.RepoListDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.repository.RepoRepository;
import com.knu.coment.service.GithubRepoService;
import com.knu.coment.service.UserService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "GithubRepo 컨트롤러", description = "GithubRepo API입니다.")
@RestController
@RequestMapping("/github")
public class GithubRepoController {

    private final GithubRepoService githubRepoService;
    private final RepoRepository repoRepository;
    private final UserService userService;

    public GithubRepoController(GithubRepoService githubRepoService, RepoRepository repoRepository, UserService userService) {
        this.githubRepoService = githubRepoService;
        this.repoRepository = repoRepository;
        this.userService = userService;
    }

    @Operation(summary = "깃 레포 목록 조회", description = "유저의 깃허브 레포 목록을 조회합니다.")
    @GetMapping("/repos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레포 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "레포 목록 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    }
    )
    public ResponseEntity<?> getUserRepositories(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User user = userService.findByGithubId(githubId);
        String githubAccessToken = user.getGithubAccessToken();

        List<RepoDto> repos = githubRepoService.getUserRepos(githubAccessToken).block();

        List<Long> userRepoIds = repoRepository.findRepoIdsByUserId(user.getId());

        List<RepoListDto> repoList = repos.stream()
                .filter(repo -> !userRepoIds.contains(repo.getId()))
                .map(repo -> {
                    RepoListDto dto = new RepoListDto();
                    dto.setId(repo.getId());
                    dto.setName(repo.getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                repoList
        );
    }

}
