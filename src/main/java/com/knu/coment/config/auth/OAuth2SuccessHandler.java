package com.knu.coment.config.auth;

import com.knu.coment.entity.User;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import com.knu.coment.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        String env = (session != null) ? (String) session.getAttribute("env") : null;

        String redirectBaseUrl;
        if ("dev".equals(env)) {
            redirectBaseUrl = "http://localhost:3000/token";
        } else {
            redirectBaseUrl = "https://www.comentor.store/token";
        }

        String githubId = authentication.getName();

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


        CookieUtil.addCookie(response, "accessToken", accessToken, 60 * 60 * 24);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 30);
        CookieUtil.addCookie(response, "githubAccessToken", githubAccessToken,  10 * 365 * 24 * 60 * 60);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectBaseUrl)
                .queryParam("role", user.getUserRole());
        if ("dev".equals(env)) {
            uriBuilder.queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("githubAccessToken", githubAccessToken);
        }
        String redirectUrl = uriBuilder.build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
