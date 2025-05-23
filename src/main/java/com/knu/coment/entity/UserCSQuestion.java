package com.knu.coment.entity;

import com.knu.coment.global.QuestionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
public class UserCSQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long userId;
    private Long questionId;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;
    private String fileName;
    private boolean isCorrect;

    public UserCSQuestion(Long userId, Long questionId, LocalDate date, QuestionStatus questionStatus) {
        this.userId = userId;
        this.questionId = questionId;
        this.date = date;
        this.questionStatus = questionStatus;
    }

    public void markAsDone() {
        this.questionStatus = QuestionStatus.DONE;
    }

    public void bookMark(String fileName) {
        this.fileName = fileName;
    }
    public void unBookMark() {
        this.fileName = null;
    }
    public void markAsUnCorrect() {
        this.isCorrect = false;
    }
    public void markAsCorrect() {
        this.isCorrect = true;
    }
}
