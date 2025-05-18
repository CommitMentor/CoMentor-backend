package com.knu.coment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
public class UserStudyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private LocalDate studyDate;

    @Column(nullable = false)
    private int solvedCount = 0;


    @Builder
    public UserStudyLog(Long userId, LocalDate studyDate, Integer solvedCount) {
        this.userId = userId;
        this.studyDate = studyDate;
        this.solvedCount = (solvedCount == null) ? 0 : solvedCount;
    }

    public void updateSolvedCount(int solvedCount) {
        if (solvedCount < 0) {
            throw new IllegalArgumentException("푼 문제 수는 0 이상이어야 합니다.");
        }
        this.solvedCount = solvedCount;
    }
}
