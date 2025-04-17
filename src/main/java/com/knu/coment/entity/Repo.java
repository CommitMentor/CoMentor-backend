package com.knu.coment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.knu.coment.dto.project_repo.OwnerDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Repo {
    @Id
    private Long id;

    private String name;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    private String language;

    @Embedded
    private OwnerDto owner;


    public Repo(Long id, String name, String htmlUrl, String createdAt, String language, OwnerDto owner) {
        this.id = id;
        this.name = name;
        this.htmlUrl = htmlUrl;
        this.createdAt = createdAt;
        this.language = language;
        this.owner = owner;
    }

}
