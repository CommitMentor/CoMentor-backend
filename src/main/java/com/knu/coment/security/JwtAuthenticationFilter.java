package com.knu.coment.security;

import com.knu.coment.entity.User;
import com.knu.coment.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = resolveToken(request);

        if (StringUtils.hasText(accessToken)) {
            // AccessToken 유효성 검사
            if (tokenProvider.validateToken(accessToken)) {
                setAuthentication(accessToken);
            } else {
                // 만료된 경우 Refresh Token 검증 로직 등
                String githubId = tokenProvider.getGithubIdFromToken(accessToken);
                Optional<User> optionalUser = userRepository.findByGithubIdFetchStacks(githubId);

                if (optionalUser.isPresent()) {
                    // user.getRefreshToken()이 유효하다면 -> 재발급
                    String newAccessToken = tokenProvider.reissueAccessToken(accessToken);
                    if (StringUtils.hasText(newAccessToken)) {
                        response.setHeader(AUTHORIZATION, TokenKey.TOKEN_PREFIX + newAccessToken);
                        setAuthentication(newAccessToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (!ObjectUtils.isEmpty(bearerToken) && bearerToken.startsWith(TokenKey.TOKEN_PREFIX)) {
            return bearerToken.substring(TokenKey.TOKEN_PREFIX.length());
        }
        return null;
    }
}
