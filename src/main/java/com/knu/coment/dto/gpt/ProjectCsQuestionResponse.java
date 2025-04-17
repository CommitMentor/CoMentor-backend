package com.knu.coment.dto.gpt;

import com.knu.coment.global.CSCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectCsQuestionResponse {
    private Long questionId;
    private String relatedCode;
    private CSCategory csCategory;
    private String question;
}
