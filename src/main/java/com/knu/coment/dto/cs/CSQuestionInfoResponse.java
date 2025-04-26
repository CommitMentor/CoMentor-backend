package com.knu.coment.dto.cs;

import com.knu.coment.dto.gpt.CreateFeedBackResponseDto;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.Stack;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CSQuestionInfoResponse {
    private Long dailyQuestionId;
    private String question;
    private boolean isAnswered;
    private Stack stack;
    private CSCategory csCategory;
    private List<CreateFeedBackResponseDto> answers;
}
