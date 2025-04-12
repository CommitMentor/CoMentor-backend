package com.knu.coment.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectCsQuestionResponse {
    private Long questionId;
    private String question;
}
