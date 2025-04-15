package com.knu.coment.dto.gpt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateProjectCsQuestionDto {
    @Schema(description = "프로젝트 ID", example = "1")
    private Long projectId;
    @Schema(description = "사용자 코드", example = "public User withdrawn(String githubId){User user = findByGithubId(githubId)" +
            "user.updateRole(Role.valueOf(\"WITHDRAWN\"));return userRepository.save(user);}")
    private String userCode;
    @Schema(description = "코드 파일 이름", example = "Main.java")
    private String folderName;
}
