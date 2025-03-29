package com.knu.coment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.knu.coment.entity.Repo;
import lombok.Getter;

@Getter
public class RepoDto {

    private Long id;

    private String name;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
    private String language;

    public Repo toEntity(){
        return new Repo(this.id, this.name, this.htmlUrl, this.createdAt, this.updatedAt, this.language);
    }

}
