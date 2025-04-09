package com.knu.coment.dto.project_repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RepoListDto {
    @JsonProperty("id")
    @Schema(description = "레포 아이디", example = "1")
    private Long id;
    @JsonProperty("name")
    @Schema(description = "레포 이름", example = "comentor")
    private String name;
}
