package com.knu.coment.dto.cs;

import com.knu.coment.global.CSCategory;
import com.knu.coment.global.QuestionStatus;
import com.knu.coment.global.Stack;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionListDto {
    private Long csQuestionId;
    private String question;
    private Stack stack;
    private CSCategory csCategory;
    private QuestionStatus questionStatus;
    private String fileName;

}
