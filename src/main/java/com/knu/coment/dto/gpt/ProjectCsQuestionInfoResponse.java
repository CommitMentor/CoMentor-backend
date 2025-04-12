package com.knu.coment.dto.gpt;

import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectCsQuestionInfoResponse {
    private Long questionId;
    private String userCode;
    private String question;
    private QuestionStatus questionStatus;
    private LocalDateTime createAt;
    private String fileName;
    private List<CsQuestionAnswerResponse> answers;}
