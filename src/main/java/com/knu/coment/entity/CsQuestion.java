package com.knu.coment.entity;

import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class CsQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Stack csStack;

    private String userCode;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String question;

    @CreatedDate
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = true)
    private Project project;

    @OneToMany(mappedBy = "csQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answer = new HashSet<>();

    public CsQuestion(String userCode, String question, LocalDateTime createAt, User user, Project project) {
        this.userCode = userCode;
        this.question = question;
        this.createAt = createAt;
        this.user = user;
        this.project = project;
    }
}
