package com.knu.coment.controller;

import com.knu.coment.dto.project_repo.CreateProjectDto;
import com.knu.coment.dto.project_repo.DashBoardDto;
import com.knu.coment.dto.project_repo.ResponseProjectDto;
import com.knu.coment.dto.project_repo.UpdateRepoDto;
import com.knu.coment.entity.Project;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.ProjectService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PROJECT 컨트롤러", description = "PROJECT API입니다.")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성", description = "프로젝트를 생성하는 API입니다.")
    @PostMapping("")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "프로젝트 생성 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<ResponseProjectDto>> createProject(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody CreateProjectDto createProjectDto) {
        String githubId = userDetails.getUsername();
        Project createdProject = projectService.createProject(githubId, createProjectDto);
        ResponseProjectDto responseDto = new ResponseProjectDto();
        responseDto.setId(createdProject.getId());

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                responseDto);
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트를 수정하는 API입니다.")
    @PutMapping("")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "프로젝트 수정 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<UpdateRepoDto>> updateProject(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long projectId,
        @RequestBody UpdateRepoDto updateRepoDto ) {
        String githubId = userDetails.getUsername();
        projectService.updateProject(githubId, projectId, updateRepoDto);
        return ApiResponseUtil.createSuccessResponse(
            SuccessCode.UPDATE_SUCCESS.getMessage());
    }

    @Operation(summary = "대시보드", description = "프로젝트를 조회하는 API입니다.")
    @GetMapping("")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "프로젝트 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<List<DashBoardDto>>> getProjects(
        @AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        List<DashBoardDto> dashBoardDtos = projectService.getUserProjects(githubId);
        return ApiResponseUtil.createSuccessResponse(
            SuccessCode.SELECT_SUCCESS.getMessage(),
                dashBoardDtos);
    }
}
