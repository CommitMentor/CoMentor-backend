package com.knu.coment.controller;

import com.knu.coment.dto.RepoDto;
import com.knu.coment.entity.User;
import com.knu.coment.service.GithubRepoService;
import com.knu.coment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/github")
public class GithubRepoController {

    private final GithubRepoService githubRepoService;
    private final UserService userService;

    public GithubRepoController(GithubRepoService githubRepoService, UserService userService) {
        this.githubRepoService = githubRepoService;
        this.userService = userService;
    }

    @Operation(summary = "깃 레포", description = "회원가입 시 필수 추가 정보를 등록 API 입니다.")
    @GetMapping("/repos")
    public ResponseEntity<?> getUserRepositories(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User user = userService.findByGithubId(githubId);
        String githubAccessToken = user.getGithubAccessToken();

        List<RepoDto> repos = githubRepoService.getUserRepos(githubAccessToken).block();
        List<String> repoNames = repos.stream()
                .map(RepoDto::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(repoNames);
    }
}
