package com.knu.coment.entity;

import com.knu.coment.global.QuestionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private QuestionStatus questionStatus;

    public UserCSQuestion(Long userId, Long questionId, LocalDate date, QuestionStatus questionStatus) {
        this.userId = userId;
        this.questionId = questionId;
        this.date = date;
        this.questionStatus = questionStatus;
    }

    public void markAsDone() {
        this.questionStatus = QuestionStatus.DONE;
    }

}
