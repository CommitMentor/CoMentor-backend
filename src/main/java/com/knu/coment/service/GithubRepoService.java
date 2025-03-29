package com.knu.coment.service;

import com.knu.coment.dto.RepoDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GithubRepoService {

    private final WebClient webClient = WebClient.builder().baseUrl("https://api.github.com").build();

    public Mono<List<RepoDto>> getUserRepos(String githubAccessToken) {
        return webClient.get()
                .uri("/user/repos")
                .header("Authorization", "token " + githubAccessToken)
                .retrieve()
                .bodyToFlux(RepoDto.class)
                .collectList();
    }
}

