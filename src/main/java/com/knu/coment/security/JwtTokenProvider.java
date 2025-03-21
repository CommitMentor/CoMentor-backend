package com.knu.coment.security;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private final UserDetailsService userDetailsService;

    // Access Token 유효기간(예: 1일)
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24;

    // Refresh Token 유효기간(예: 7일)
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    /**
     * Access Token 발급
     */
    public String createAccessToken(String githubId, String role) {
        Claims claims = Jwts.claims().setSubject(githubId);
        claims.put("role", role);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)       // 클레임 정보
                .setIssuedAt(now)        // 발급 시간
                .setExpiration(expiration) // 만료 시간
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 서명
                .compact();
    }

    /**
     * Refresh Token 발급
     * - 일반적으로 클레임 정보는 최소화하는 편
     */
    public String createRefreshToken(String githubId) {
        Claims claims = Jwts.claims().setSubject(githubId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 토큰에서 GitHub ID 추출
     */
    public String getGithubIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 내부적으로 파싱 가능한지 + 만료 체크
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 인증 정보 가져옴
     * - UserDetailsService를 활용
     */
    public Authentication getAuthentication(String token) {
        String githubId = getGithubIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(githubId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * Claims 파싱
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Refresh Token이 유효한지 확인 후 Access Token 재발급
     * - 실제론 DB나 Redis 등에 저장된 Refresh Token과 비교해
     *   아직 유효한 지, 탈취/무효화 여부를 확인해야 함
     */
    public String reissueAccessToken(String refreshToken) {
        // 우선 refreshToken 자체가 파싱 가능한지 확인
        if (validateToken(refreshToken)) {
            // 유효하면 GitHub ID 추출해서 새 Access Token 생성
            String githubId = getGithubIdFromToken(refreshToken);
            // DB에서 권한 정보를 조회해 올 수도 있음
            String role = "USER";
            return createAccessToken(githubId, role);
        }
        return null;
    }
}
