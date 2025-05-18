package com.knu.coment.controller;

import com.knu.coment.dto.DailyStudyDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.UserService;
import com.knu.coment.service.UserStudyLogService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "UserStudyLog 컨트롤러", description = "UserStudyLog API입니다.")
@Controller
@RequiredArgsConstructor
@RequestMapping("/log")
public class UserStudyLogController {
    private final UserStudyLogService userStudyLogService;
    private final UserService userService;

    @Operation(summary = "최근 10일간 학습 기록 조회", description = "오늘을 포함하여 최근 10일 동안 날짜별로 문제를 푼 개수를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/history")
    public ResponseEntity<?> getLast10DaysStudy(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User user = userService.findByGithubId(githubId);
        List<DailyStudyDto> result = userStudyLogService.getLast10DaysStudy(user.getId());
        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                result);
    }

    @Operation(summary = "연속 학습일 수 조회", description = "오늘을 기준으로 연속해서 문제를 푼 일 수를 계산해 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping()
    public ResponseEntity<?> getStudyStreak(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User user = userService.findByGithubId(githubId);
        int streak = userStudyLogService.getStudyStreak(user.getId());
        return ApiResponseUtil.ok(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                streak);
    }
}
