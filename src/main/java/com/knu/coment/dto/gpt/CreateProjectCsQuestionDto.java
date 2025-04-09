package com.knu.coment.dto.gpt;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateProjectCsQuestionDto {
    private Long projectId;
    private String userCode;
}
