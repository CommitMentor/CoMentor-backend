package com.knu.coment.controller;

import com.knu.coment.dto.gpt.*;
import com.knu.coment.entity.CsQuestion;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.CsQuestionService;
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
import java.util.stream.Collectors;

@Tag(name = "CSQuestion 컨트롤러", description = "CS Question 생성 API입니다.")
@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class CsQuestionController {
    private final CsQuestionService csQuestionService;

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
        String processedUserCode = dto.getUserCode()
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        List<CsQuestion> questions = csQuestionService.createProjectQuestions(
                githubId,
                dto.getProjectId(),
                processedUserCode
        );
        List<ProjectCsQuestionResponse> responseList = questions.stream()
                .map(q -> new ProjectCsQuestionResponse(q.getId(), q.getQuestion()))
                .collect(Collectors.toList());

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage(),
                responseList
        );
    }

    @Operation(summary = "프로젝트 CS 질문 목록 조회", description = "프로젝트 CS 질문 목록을 조회합니다.")
    @GetMapping("/project/list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 목록 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCsQuestionList(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestParam Long projectId) {
        String githubId = userDetails.getUsername();
        List<CsQuestionGroupDto> csQuestionsList = csQuestionService.getGroupedCsQuestions(githubId, projectId);

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                csQuestionsList
        );
    }

    @Operation(summary = "PROJECT CS 질문 기록 상세 조회", description = "PROJECT CS 질문 상세 기록을 조회합니다.")
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
        ProjectCsQuestionInfoResponse projectCsQuestionResponse = csQuestionService.getCsQuestionDetail(githubId, csQuestionId);

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                projectCsQuestionResponse
        );
    }
}
