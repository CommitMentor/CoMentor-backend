package com.knu.coment.dto.project_repo;

import com.knu.coment.global.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateRepoDto {
    @Schema(description = "프로젝트 설명", example = "안녕 나는 코멘토")
    private String description;
    @Schema(description = "프로젝트 역할", example = "안녕 나는 백엔드")
    private String role;
    @Schema(description = "프로젝트 상태", example = "PROGRESS")
    @Enumerated(EnumType.STRING)
    private Status status;
}
