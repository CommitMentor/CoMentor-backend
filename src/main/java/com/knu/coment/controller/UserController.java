package com.knu.coment.controller;

import com.knu.coment.dto.UserUpdateDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.service.UserService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Tag(name = "USER 컨트롤러", description = "USER API입니다.")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "회원가입 시 필수 추가 정보를 등록 API 입니다.")
    @PostMapping("/join")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "404", description = "회원가입 실패", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = Api_Response.class)))
    })
    public ResponseEntity<Api_Response<UserUpdateDto>> joinUser(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody UserUpdateDto userUpdateDto) {

        String githubId = userDetails.getUsername();
        userService.join(githubId, userUpdateDto);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage());
    }

    @Operation(summary = "유저 정보 조회", description = "유저 정보 조회 API 입니다.")
    @GetMapping("/info")
    @ApiResponses (value = {
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = Api_Response.class)))
    })
    public ResponseEntity<Api_Response<UserUpdateDto>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        UserUpdateDto userInfo = userService.getUserInfo(githubId);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.SELECT_SUCCESS.getMessage(),
                userInfo);
    }

    @Operation(summary = "유저 정보 수정", description = "유저 정보 수정 API 입니다.")
    @PutMapping("/info")
    @ApiResponses (value = {
            @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = Api_Response.class)))
    })
    public ResponseEntity<Api_Response<UserUpdateDto>> updateUserInfo(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @RequestBody UserUpdateDto userUpdateDto) {
        String githubId = userDetails.getUsername();
        userService.updateInfo(githubId, userUpdateDto);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage());
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴 API 입니다.")
    @PutMapping("/withdrawn")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "404", description = "회원탈퇴 실패", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = Api_Response.class)))
    })
    public ResponseEntity<?> withdrawn(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User updatedUser = userService.withdrawn(githubId);

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage(), updatedUser.getRefreshToken());
    }

    @Operation(summary = "리프레시 토큰 재발급", description = "Refresh Token 재발급 API 입니다.")
    @PostMapping("/refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh Token 재발급 성공", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "404", description = "재발급 실패", content = @Content(schema = @Schema(implementation = Api_Response.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = Api_Response.class)))
    })
    public ResponseEntity<?> refreshAccessToken(@AuthenticationPrincipal UserDetails userDetails) {
        String githubId = userDetails.getUsername();
        User updatedUser = userService.renewRefreshToken(githubId);

        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage(), updatedUser.getRefreshToken());
    }

}
