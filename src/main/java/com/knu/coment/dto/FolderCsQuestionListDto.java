package com.knu.coment.dto;

import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderCsQuestionListDto {
    private String folderName;
    private Long questionId;
    private String question;
    private QuestionStatus questionStatus;
}
