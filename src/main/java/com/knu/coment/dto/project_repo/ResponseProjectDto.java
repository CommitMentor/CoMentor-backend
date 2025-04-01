package com.knu.coment.dto.project_repo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseProjectDto {
    @Schema(description = "프로젝트 아이디", example = "1")
    private Long id;
}
