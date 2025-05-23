package com.knu.coment.controller;

import com.knu.coment.dto.project_repo.CreateProjectDto;
import com.knu.coment.dto.project_repo.DashBoardDto;
import com.knu.coment.dto.project_repo.UpdateRepoDto;
import com.knu.coment.global.Status;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.ProjectService;
import com.knu.coment.util.ApiResponseUtil;
import com.knu.coment.util.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<?> createProject(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody CreateProjectDto createProjectDto) {
        String githubId = userDetails.getUsername();
        projectService.createProject(githubId, createProjectDto);

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage());
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
        return ApiResponseUtil.ok(
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
    public ResponseEntity<Api_Response<PageResponse<DashBoardDto>>> getProjects(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) Status status,
        @RequestParam int page) {
        String githubId = userDetails.getUsername();
        PageResponse<DashBoardDto> dashBoardDtos = projectService.getUserProjects(githubId, status, page);
        return ApiResponseUtil.ok(
            SuccessCode.SELECT_SUCCESS.getMessage(),
                dashBoardDtos);
    }
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제하는 API입니다.")
    @DeleteMapping("")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "프로젝트 삭제 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> deleteProject(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long projectId) {
        String githubId = userDetails.getUsername();
        projectService.deleteProject(githubId, projectId);
        return ApiResponseUtil.ok(
            SuccessCode.DELETE_SUCCESS.getMessage());
    }
    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트를 상세 조회하는 API입니다.")
    @GetMapping("/info")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "프로젝트 상세 조회 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<DashBoardDto>> getProjectInfo(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long projectId) {
        String githubId = userDetails.getUsername();
        DashBoardDto dashBoardDto = projectService.getProjectInfo(githubId, projectId);
        return ApiResponseUtil.ok(
            SuccessCode.SELECT_SUCCESS.getMessage(),
                dashBoardDto);
    }
}