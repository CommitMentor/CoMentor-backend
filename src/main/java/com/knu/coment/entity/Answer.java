package com.knu.coment.entity;

import com.knu.coment.global.Author;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String content;

    private LocalDateTime answeredAt;

    @Enumerated(EnumType.STRING)
    private Author author;

    @ManyToOne
    @JoinColumn(name = "csQuestion_id", nullable = true)
    private CsQuestion csQuestion;

    public Answer(String content, LocalDateTime answeredAt, Author author, CsQuestion csQuestion) {
        this.content = content;
        this.answeredAt = answeredAt;
        this.author = author;
        this.csQuestion = csQuestion;
    }
}
