package com.knu.coment.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CsQuestionAnswerResponse {
    private String content;
    private LocalDateTime answeredAt;
    private String author;
}
