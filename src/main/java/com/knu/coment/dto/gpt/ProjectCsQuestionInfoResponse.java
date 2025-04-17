package com.knu.coment.dto.gpt;

import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectCsQuestionInfoResponse {
    private Long questionId;
    private CSCategory csCategory;;
    private String relatedCode;
    private String question;
    private QuestionStatus questionStatus;
    private String folderName;
    private List<CreateFeedBackResponseDto> answers;}
