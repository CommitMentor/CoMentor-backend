package com.knu.coment.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThirdCsQuestionListDto {
    Long questionId;
    String question;
    String userCode;
}
