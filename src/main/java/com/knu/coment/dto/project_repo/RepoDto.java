package com.knu.coment.dto.project_repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.knu.coment.entity.Repo;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RepoDto {

    private Long id;

    private String name;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    private String language;

    private OwnerDto owner;

    public Repo toEntity(){
        return new Repo(this.id, this.name, this.htmlUrl, this.createdAt, this.language, this.owner);
    }

    public RepoDto(Long id, String name, String htmlUrl, String language, OwnerDto owner) {
        this.id = id;
        this.name = name;
        this.htmlUrl = htmlUrl;
        this.language = language;
        this.owner = owner;
    }

}
