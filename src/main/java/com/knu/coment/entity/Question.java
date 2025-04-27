package com.knu.coment.entity;

import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.QuestionType;
import com.knu.coment.global.Stack;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CSCategory csCategory;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String relatedCode;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String question;

    @CreatedDate
    private LocalDateTime createAt;

    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    private Stack stack;

    private String folderName;

    private Long userId;
    private Long projectId;
    private Long folderId;

    public Question(CSCategory csCategory, QuestionType questionType, String relatedCode, String question, LocalDateTime createAt, QuestionStatus questionStatus, String folderName, Long folderId, Long userId, Long projectId) {

        this.csCategory = csCategory;
        this.questionType = questionType;
        this.relatedCode = relatedCode;
        this.question = question;
        this.createAt = createAt;
        this.questionStatus = questionStatus;
        this.folderName = folderName;
        this.folderId = folderId;
        this.userId = userId;
        this.projectId = projectId;
    }
    public Question(CSCategory csCategory, QuestionType questionType, String question, QuestionStatus questionStatus, Stack stack) {
        this.csCategory = csCategory;
        this.questionType = questionType;
        this.question = question;
        this.questionStatus = questionStatus;
        this.stack = stack;
    }

    public void markAsDone() {
        this.questionStatus = QuestionStatus.DONE;
    }

    public void bookMark(Long folderId) {
        this.folderId = folderId;
    }

    public void unBookMark() {
        this.folderId = null;
    }

}
