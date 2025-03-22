package com.knu.coment.config;

import com.knu.coment.config.auth.CustomOAuth2UserService;
import com.knu.coment.config.auth.OAuth2SuccessHandler;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/login/oauth2/github"),
                                new AntPathRequestMatcher("/login/**"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                new AntPathRequestMatcher("/**")

                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler())
                );

        return http.build();
    }
    private AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtTokenProvider, userService);
    }
}
