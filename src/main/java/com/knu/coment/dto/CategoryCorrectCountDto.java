package com.knu.coment.dto;

import com.knu.coment.global.CSCategory;

public record CategoryCorrectCountDto(
        CSCategory category,
        long correctCount,
        long incorrectCount
) {}
