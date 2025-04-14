package com.knu.coment.dto.gpt;

import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectCsQuestionInfoResponse {
    private Long questionId;
    private String userCode;
    private String question;
    private QuestionStatus questionStatus;
    private String fileName;
    private List<CreateFeedBackResponseDto> answers;}
