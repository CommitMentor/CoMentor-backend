package com.knu.coment.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateFeedBackResponseDto {
    private String content;
    private String author;
}
