package com.knu.coment.controller;

import com.knu.coment.dto.CategoryCorrectCountDto;
import com.knu.coment.dto.gpt.*;
import com.knu.coment.entity.Question;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.ProjectQuestionService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "PROJECT CSQuestion 컨트롤러", description = "PROJECT CS Question API입니다.")
@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class ProjectCsQuestionController {
    private final ProjectQuestionService projectQuestionService;

    @Operation(summary = "프로젝트 CS 질문 생성", description = "프로젝트 CS 질문을 생성합니다.")
    @PostMapping("/project" )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 생성 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> createQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestBody CreateProjectCsQuestionDto dto) {
        String githubId = userDetails.getUsername();
        List<Question> questions = projectQuestionService.createProjectQuestions(
                githubId,
                dto.getProjectId(),
                dto.getUserCode(),
                dto.getFolderName()
        );
        List<ProjectCsQuestionResponse> responseList = questions.stream()
                .map(q -> new ProjectCsQuestionResponse(q.getId(), q.getRelatedCode(),q.getCsCategory(), q.getQuestion()))
                .collect(Collectors.toList());

        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                responseList
        );
    }

    @Operation(summary = "[질문 기록] 프로젝트 CS 질문 기록 목록 조회", description = "프로젝트 CS 질문 기록을 조회합니다.")
    @GetMapping("/project/list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 목록 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCsQuestionList(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestParam Long projectId,
                                                      @RequestParam(required = false) CSCategory category) {
        String githubId = userDetails.getUsername();
        List<CsQuestionListDto> csQuestionsList = projectQuestionService.getGroupedCsQuestions(githubId, projectId, category);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                csQuestionsList
        );
    }

    @Operation(summary = "[질문 기록] PROJECT CS 질문 기록 상세 조회", description = "PROJECT CS 질문 상세 기록을 조회합니다.")
    @GetMapping("/project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 상세 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCsQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam Long csQuestionId) {
        String githubId = userDetails.getUsername();
        ProjectCsQuestionInfoResponse projectCsQuestionResponse = projectQuestionService.getCsQuestionDetail(githubId, csQuestionId);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                projectCsQuestionResponse
        );
    }
    @Operation(summary = "카테고리별 질문 수 조회", description = "카테고리별 질문 수 조회 API입니다")
    @GetMapping("/project/category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 질문 수 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "카테고리별 질문 수 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCategoryCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        Map<String, Long> count = projectQuestionService.getSolvedCountByCategory(githubId);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                count
        );
    }
    @Operation(summary ="카테고리 별 오답 수 조회", description = "카테고리 별 오답 수 조회 API입니다")
    @GetMapping("/project/category/correct")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 별 오답 수 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "카테고리 별 오답 수 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCategoryCorrectCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        List<CategoryCorrectCountDto> stats = projectQuestionService.getCategoryStatsByUser(githubId);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                stats
        );
    }
}
