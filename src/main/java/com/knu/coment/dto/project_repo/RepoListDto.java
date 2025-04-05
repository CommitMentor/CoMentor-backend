package com.knu.coment.dto.project_repo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepoListDto {
    @Schema(description = "레포 아이디", example = "1")
    private Long id;
    @Schema(description = "레포 이름", example = "comentor")
    private String name;
}
