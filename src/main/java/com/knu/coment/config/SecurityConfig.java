package com.knu.coment.config;

import com.knu.coment.config.auth.CustomOAuth2UserService;
import com.knu.coment.config.auth.OAuth2SuccessHandler;
import com.knu.coment.global.Role;
import com.knu.coment.security.JwtAccessDeniedHandler;
import com.knu.coment.security.JwtAuthenticationEntryPoint;
import com.knu.coment.security.JwtAuthenticationFilter;
import com.knu.coment.security.JwtTokenProvider;
import com.knu.coment.service.UserService;
import com.knu.coment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())

                .cors(cors -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Arrays.asList("https://comentor.store", "http://localhost:3000", "http://localhost:8080","https://comentor.vercel.app"));
                    configuration.setAllowedMethods(Arrays.asList("*"));
                    configuration.setAllowedHeaders(Arrays.asList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setMaxAge(3600L);
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", configuration);
                    cors.configurationSource(source);
                })

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/login/oauth2/github"),
                                new AntPathRequestMatcher("/login/**"),
                                new AntPathRequestMatcher("/auth/**"),
                                new AntPathRequestMatcher("/user/join"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/v3/api-docs/**")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/user/**"),
                                        new AntPathRequestMatcher("/project/**"),
                                        new AntPathRequestMatcher("/github/**"),
                                        new AntPathRequestMatcher("/folder/**"),
                                        new AntPathRequestMatcher("/question/**"),
                                        new AntPathRequestMatcher("/feedback/**"),
                                        new AntPathRequestMatcher("/push/**"),
                                        new AntPathRequestMatcher("/notifications/**")
                        ).hasAuthority(Role.USER.getKey())
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler())
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtTokenProvider, userService, authorizedClientService);
    }
}
