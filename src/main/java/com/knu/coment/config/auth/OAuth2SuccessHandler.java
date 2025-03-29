package com.knu.coment.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knu.coment.entity.User;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String githubId = authentication.getName();

        // Access Token과 Refresh Token 발급
        String accessToken = tokenProvider.createAccessToken(githubId, authentication.getAuthorities().toString());
        String refreshToken = tokenProvider.createRefreshToken(githubId);

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );
        String githubAccessToken = client.getAccessToken().getTokenValue();
        User user = userService.findByGithubId(githubId);
        user.updateRefreshToken(refreshToken);
        user.updateGithubAccessToken(githubAccessToken);
        userService.saveUser(user);

        String cookieValue = URLEncoder.encode("Bearer " + accessToken, StandardCharsets.UTF_8.toString());
        Cookie accessTokenCookie = new Cookie("Authorization", cookieValue);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("role", String.valueOf(user.getUserRole()));
        response.setContentType("application/json;charset=UTF-8");

        new ObjectMapper().writeValue(response.getWriter(), responseBody);
    }
}
