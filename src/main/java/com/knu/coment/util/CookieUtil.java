package com.knu.coment.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        try {
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            Cookie cookie = new Cookie(name, encodedValue);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
        } catch (Exception e) {
            throw new RuntimeException("쿠키 설정 오류", e);
        }
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    public static void updateCookie(HttpServletResponse response, String name, String newValue, int maxAge) {
        deleteCookie(response, name);
        addCookie(response, name, newValue, maxAge);
    }

    // 쿠키 삭제
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}

