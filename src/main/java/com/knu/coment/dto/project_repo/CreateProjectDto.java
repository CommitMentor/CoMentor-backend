package com.knu.coment.dto.project_repo;

import com.knu.coment.entity.Project;
import com.knu.coment.global.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CreateProjectDto {
    @Schema(description = "레포 ID", example = "1")
    private Long id;
    @Schema(description = "프로젝트 설명", example = "안녕 나는 코멘토")
    private String description;
    @Schema(description = "프로젝트 역할", example = "안녕 나는 백엔드")
    private String role;
    @Schema(description = "프로젝트 상태", example = "PROGRESS")
    @Enumerated(EnumType.STRING)
    private Status status;

    public Project toEntity(){
        return new Project(this.description, this.role, this.status);
    }
    public CreateProjectDto(Long id, String description, String role, Status status) {
        this.id = id;
        this.description = description;
        this.role = role;
        this.status = status;
    }
}
