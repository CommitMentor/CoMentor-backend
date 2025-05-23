package com.knu.coment.controller;

import com.knu.coment.dto.CategoryCorrectCountDto;
import com.knu.coment.dto.cs.CSDashboard;
import com.knu.coment.dto.cs.CSQuestionInfoResponse;
import com.knu.coment.global.CSCategory;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.CSQuestionService;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Tag(name = "CSQuestion 컨트롤러", description = "CS Question API입니다.")
@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class CsQuestionController {
    private final CSQuestionService csQuestionService;

    @Operation(summary = "CS 연습하기 대시보드", description = "CS 연습하기 대시보드 API입니다")
    @GetMapping("list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 목록 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<PageResponse<CSDashboard>>> getQuestionList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int page,
            @RequestParam(required = false) CSCategory csCategory) {
        String githubId = userDetails.getUsername();
        PageResponse<CSDashboard> csDashboard =csQuestionService.getDashboard(githubId,page, csCategory);
        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                csDashboard
        );
    }
    @Operation(summary = "CS 질문 상세 조회", description = "CS 질문 상세 조회 API입니다")
    @GetMapping()
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CS 질문 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "CS 질문 상세 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Api_Response<CSQuestionInfoResponse>> getQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long csQuestionId) {
        String githubId = userDetails.getUsername();
        CSQuestionInfoResponse csQuestionInfoResponse = csQuestionService.getCSQuestionDetail(githubId, csQuestionId);
        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                csQuestionInfoResponse
        );
    }
    @Operation(summary = "카테고리별 질문 수 조회", description = "카테고리별 질문 수 조회 API입니다")
    @GetMapping("/category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 질문 수 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "카테고리별 질문 수 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getCategoryCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        Map<String, Long> count = csQuestionService.getSolvedCountByCategory(githubId);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                count
        );
    }
    @Operation(summary ="카테고리 별 오답 수 조회", description = "카테고리 별 오답 수 조회 API입니다")
    @GetMapping("/category/correct")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 별 오답 수 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "카테고리 별 오답 수 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getProjectCategoryCorrectCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        List<CategoryCorrectCountDto> stats = csQuestionService.getCategoryStatsByUser(githubId);

        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                stats
        );
    }
}
