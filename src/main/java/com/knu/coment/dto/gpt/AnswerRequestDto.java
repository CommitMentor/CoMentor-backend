package com.knu.coment.dto.gpt;

import com.knu.coment.global.Author;
import lombok.Data;

@Data
public class AnswerRequestDto {
    private String answer;
    private Author author;
    private Long csQuestionId;
}
