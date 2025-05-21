package com.knu.coment.controller;

import com.knu.coment.dto.UserNotificationResponse;
import com.knu.coment.entity.User;
import com.knu.coment.entity.UserNotification;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.repository.UserNotificationRepository;
import com.knu.coment.service.UserService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "USER NOTIFICATION 컨트롤러", description = "USER NOTIFICATION API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class UserNotificationController {
    private final UserNotificationRepository userNotificationRepository;
    private final UserService userService;

    @Operation(summary = "알림 목록 조회", description = "알림 목록 조회 API 입니다")
    @GetMapping()
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "알림 목록 조회 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public List<?> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User user = userService.findByGithubId(githubId);
        return userNotificationRepository.findByUserIdOrderBySentAtDesc(user.getId())
                .stream()
                .map(UserNotificationResponse::from)
                .toList();
    }

    @Operation(summary = "알림 수신 확인", description = "알림 수신 확인 여부 변경 API 입니다")
    @PostMapping("/read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 수신 확인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "알림 수신 확인 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @Transactional
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Long id) {
        userNotificationRepository.findById(id).ifPresent(UserNotification::markAsRead);
        return ApiResponseUtil.ok(
                SuccessCode.UPDATE_SUCCESS.getMessage()
        );
    }
}
