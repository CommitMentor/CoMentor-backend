package com.knu.coment.dto.gpt;

import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectQuestionListDto {
    private Long questionId;
    private String question;
    private QuestionStatus questionStatus;
}
