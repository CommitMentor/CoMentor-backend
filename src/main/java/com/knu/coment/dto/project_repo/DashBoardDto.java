package com.knu.coment.dto.project_repo;

import com.knu.coment.entity.Project;
import com.knu.coment.entity.Repo;
import com.knu.coment.global.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardDto {
    @Schema(description = "프로젝트 아이디", example = "1")
    private Long id;
    @Schema(description = "레포 이름", example = "comentor")
    private String name;
    @Schema(description = "레포 언어", example = "Java")
    private String language;
    @Schema(description = "레포 설명", example = "코멘터 프로젝트")
    private String description;
    @Schema(description = "레포 역할", example = "백엔드")
    private String role;
    @Schema(description = "레포 상태", example = "PROGRESS")
    private Status status;
    @Schema(description = "레포 생성일", example = "2021-09-01")
    private String updatedAt;
    @Schema(description = "레포 소유자", example = "코멘토")
    private String login;

    public static DashBoardDto fromEntity(Project project) {
        Repo repo = project.getRepo();
        return new DashBoardDto(
                project.getId(),
                (repo != null) ? repo.getName() : null,
                (repo != null) ? repo.getLanguage() : null,
                project.getDescription(),
                project.getRole(),
                project.getStatus(),
                (repo != null) ? repo.getUpdatedAt() : null,
                (repo != null && repo.getOwner() != null) ? repo.getOwner().getLogin() : null
        );
    }
}

