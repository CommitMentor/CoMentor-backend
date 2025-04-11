package com.knu.coment.dto.gpt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FeedBackRequestDto {
    @Schema(description = "사용자 답변 내용", example = "사용자의 깃허브 커밋 내역을 분석하여 CS 질문을 생성하는 프로젝트입니다.")
    private String answer;
    @Schema(description = "CS 질문 ID", example = "1")
    private Long csQuestionId;
}
