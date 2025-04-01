package com.knu.coment.dto.project_repo;

import com.knu.coment.global.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardDto {
    @Schema(description = "레포 이름", example = "comentor")
    private String name;
    @Schema(description = "레포 언어", example = "Java")
    private String language;
    @Schema(description = "레포 설명", example = "코멘터 프로젝트")
    private String description;
    @Schema(description = "레포 상태", example = "PROGRESS")
    private Status status;
    @Schema(description = "레포 생성일", example = "2021-09-01")
    private String updatedAt;
}
