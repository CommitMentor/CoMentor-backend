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
        // accessToken, refreshToken 발급
        // 1) 사용자 정보 확인 (Authentication에서 githubId 추출)
        String githubId = authentication.getName();

        // 2) Access Token, Refresh Token 생성
        String accessToken = tokenProvider.createAccessToken(githubId, authentication.getAuthorities().toString());
        String refreshToken = tokenProvider.createRefreshToken(githubId);

        // 3) Refresh Token을 DB에 저장
        User user = userService.findByGithubId(githubId);
        user.updateRefreshToken(refreshToken);
        userService.saveUser(user);

        response.setHeader("Authorization", "Bearer " + accessToken);  // Authorization 헤더에 Access Token 추가

        // 4) 필요한 경우 Refresh Token을 쿠키로 전달하거나, DB만 저장하고 Access Token만 리다이렉트로 전달
        String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }

}