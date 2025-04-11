package com.knu.coment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class OAuth2Controller {
    @GetMapping("/auth/github/env")
    public void storeEnvAndRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String envParam = request.getParameter("env");
        HttpSession session = request.getSession();
        session.setAttribute("env", envParam);

        response.sendRedirect("/oauth2/authorization/github");
    }
}
