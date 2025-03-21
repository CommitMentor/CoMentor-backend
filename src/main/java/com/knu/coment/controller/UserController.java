package com.knu.coment.controller;

import com.knu.coment.dto.UserUpdateDto;
import com.knu.coment.entity.User;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.SuccessCode;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.security.TokenKey;
import com.knu.coment.service.UserService;
import com.knu.coment.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "회원가입 시 필수 추가 정보를 등록 API 입니다.")
    @PostMapping("/join")
    public ResponseEntity<Api_Response<UserUpdateDto>> joinUser(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody UserUpdateDto userUpdateDto) {

        String githubId = userDetails.getUsername();
        userService.join(githubId, userUpdateDto);
        return ApiResponseUtil.createSuccessResponse(
                SuccessCode.INSERT_SUCCESS.getMessage());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader(name = AUTHORIZATION, required = false) String refreshHeader) {
        if (!StringUtils.hasText(refreshHeader) || !refreshHeader.startsWith(TokenKey.TOKEN_PREFIX)) {
            return ResponseEntity.badRequest().body("Refresh Token이 존재하지 않거나 형식이 올바르지 않습니다.");
        }
        String refreshToken = refreshHeader.substring(TokenKey.TOKEN_PREFIX.length());
        String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken);
        if (!StringUtils.hasText(newAccessToken)) {
            return ResponseEntity.status(401).body("Refresh Token이 만료되었거나 유효하지 않습니다.");
        }
        return ResponseEntity.ok(newAccessToken);
    }

//    @GetMapping("/info")
//    public ResponseEntity<Api_Response<UserUpdateDto>> getUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
//        String githubId = oAuth2User.getAttribute("login");
//        UserUpdateDto userUpdateDto = userService.getUserInfo(githubId);
//        return ApiResponseUtil.createSuccessResponse(
//                SuccessCode.SELECT_SUCCESS.getMessage(),
//                userUpdateDto);
//    }

}
