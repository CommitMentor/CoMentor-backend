package com.knu.coment.dto.gpt;

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
    private LocalDateTime createAt;
    private List<CsQuestionAnswerResponse> answers;}
