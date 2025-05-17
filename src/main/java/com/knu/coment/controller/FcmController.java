package com.knu.coment.controller;

import com.knu.coment.dto.FcmTokenDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.FcmService;
import com.knu.coment.service.UserService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM 컨트롤러", description = "FCM API입니다.")
@Controller
@RequestMapping("/push")
@AllArgsConstructor
public class FcmController {
    private final FcmService fcmService;
    private final UserService userService;

    @Operation(summary = "FCM 토큰 등록", description = "FCM 토큰을 등록하는 API입니다.")
    @PostMapping("/register")
    @ApiResponses ( value = {
            @ApiResponse(responseCode = "200", description = "FCM 토큰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "FCM 토큰 등록 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> registerToken(@AuthenticationPrincipal UserDetails userDetails, @RequestBody FcmTokenDto dto) {
        User user = userService.findByGithubId(userDetails.getUsername());
        fcmService.saveOrUpdateFcmToken(user.getId(), dto.getFcmToken());
        return ApiResponseUtil.ok(
                SuccessCode.INSERT_SUCCESS.getMessage()
        );
    }

    @Operation(summary = "FCM 토큰 삭제", description = "FCM 토큰을 삭제하는 API입니다.")
    @DeleteMapping("/logout")
    @ApiResponses ( value = {
            @ApiResponse(responseCode = "200", description = "FCM 토큰 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "FCM 토큰 삭제 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> deleteToken(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String token) {
        fcmService.deleteToken(token);
        return ApiResponseUtil.ok(
                SuccessCode.DELETE_SUCCESS.getMessage()
        );
    }

}
