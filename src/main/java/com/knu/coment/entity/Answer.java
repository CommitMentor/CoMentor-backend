package com.knu.coment.entity;

import com.knu.coment.global.Author;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
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
