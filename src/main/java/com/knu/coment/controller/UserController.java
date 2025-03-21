package com.knu.coment.controller;

import com.knu.coment.dto.UserUpdateDto;
import com.knu.coment.entity.User;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.security.TokenKey;
import com.knu.coment.service.UserService;
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

    @PostMapping("/join")
    public ResponseEntity<?> joinUser(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody UserUpdateDto userUpdateDto) {
        if (userDetails == null || userDetails.getUsername() == null) {
            return ResponseEntity.badRequest().body("로그인 정보가 없습니다.");
        }

        String githubId = userDetails.getUsername();
        User existingUser = userService.findByGithubId(githubId);
        if (existingUser == null) {
            return ResponseEntity.badRequest().body("해당 GitHub ID의 사용자가 존재하지 않습니다.");
        }

        // userService.join() 통해 가입 완료
        User updatedUser = userService.join(existingUser.getId(), userUpdateDto);
        return ResponseEntity.ok(updatedUser);
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

    @GetMapping("/info")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null || oAuth2User.getAttribute("id") == null) {
            return ResponseEntity.badRequest().body("로그인 정보가 없습니다.");
        }

        String githubId = oAuth2User.getAttribute("id").toString();
        // DB에서 찾아서 전체 User 정보 반환 (DTO 변환 권장)
        User user = userService.findByGithubId(githubId);
        return ResponseEntity.ok(user);
    }

}
