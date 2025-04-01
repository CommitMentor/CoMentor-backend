package com.knu.coment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Repo {
    @Id
    private Long id;

    private String name;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
    private String language;

    @OneToMany(mappedBy = "repo")
    private List<Project> projects = new ArrayList<>();

    public Repo(Long id, String name, String htmlUrl, String createdAt, String updatedAt, String language) {
        this.id = id;
        this.name = name;
        this.htmlUrl = htmlUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
    }
}
