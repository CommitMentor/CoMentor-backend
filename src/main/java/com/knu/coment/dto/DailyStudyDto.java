package com.knu.coment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStudyDto {
    private LocalDate date;
    private int solvedCount;
}
