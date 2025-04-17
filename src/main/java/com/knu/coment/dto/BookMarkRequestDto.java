package com.knu.coment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookMarkRequestDto {
    @Schema(description = "북마크할 폴더 이름", example = "default")
    private String fileName;
    @Schema(description = "북마크할 CS 질문 ID", example = "1")
    private Long csQuestionId;
}
