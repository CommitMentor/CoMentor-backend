package com.knu.coment.config.auth;

import com.knu.coment.entity.User;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private static final String REDIRECT_URI = "/auth/success";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // GitHub ID 추출
        String githubId = authentication.getName();

        // Access Token과 Refresh Token 발급
        String accessToken = tokenProvider.createAccessToken(githubId, authentication.getAuthorities().toString());
        String refreshToken = tokenProvider.createRefreshToken(githubId);

        // Refresh Token을 DB에 저장
        User user = userService.findByGithubId(githubId);
        user.updateRefreshToken(refreshToken);
        userService.saveUser(user);

        // Authorization 헤더에 Access Token 추가 (이 부분은 리디렉션이 아닌 API 호출 시에 사용될 것)
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 리디렉션 URL 생성
        String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        // 리디렉션
        response.sendRedirect(redirectUrl);
    }
}
