package com.knu.coment.dto.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Getter
public class CsQuestionGroupDto {
    private LocalDate createdAt;
    private List<ProjectQuestionListDto> questions;
}
