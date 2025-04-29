package com.knu.coment.dto;

import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderCsQuestionListDto {
    private Long questionId;
    private Long csQuestionId;
    private Long projectId;
    private String question;
    private String repoName;
    private String fileName;
    private CSCategory csCategory;
    private QuestionStatus questionStatus;
}
