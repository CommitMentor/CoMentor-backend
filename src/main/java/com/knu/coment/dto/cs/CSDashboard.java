package com.knu.coment.dto.cs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CSDashboard {
    private LocalDate date;
    private List<QuestionListDto> questions;

}
