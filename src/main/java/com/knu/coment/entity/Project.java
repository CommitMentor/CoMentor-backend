package com.knu.coment.entity;

import com.knu.coment.exception.ProjectExceptionHandler;
import com.knu.coment.exception.UserExceptionHandler;
import com.knu.coment.exception.code.ProjectErrorCode;
import com.knu.coment.exception.code.UserErrorCode;
import com.knu.coment.global.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String role;
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "repo_id", nullable = false)
    private Repo repo;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CsQuestion> csQuestions = new HashSet<>();

    public Project(String description, String role, Status status) {
        this.description = description;
        this.role = role;
        this.status = status;
    }

    public void assignUser(User user) {
        if (user == null) {
            throw new UserExceptionHandler(UserErrorCode.NOT_FOUND_USER);
        }
        this.user = user;
    }

    public void assignRepo(Repo repo) {
        if (repo == null) {
            throw new ProjectExceptionHandler(ProjectErrorCode.NOT_FOUND_Repo);
        }
        this.repo = repo;
    }


    public void update(String description, String role, Status status){
        this.description = description;
        this.role = role;
        this.status = status;
    }

}
