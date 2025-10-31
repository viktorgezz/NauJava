package ru.viktorgezz.NauJava.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.viktorgezz.NauJava.auth.dto.AuthenticationRequest;
import ru.viktorgezz.NauJava.auth.dto.AuthenticationResponse;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.auth.service.AuthenticationService;
import ru.viktorgezz.NauJava.security.JwtProperties;
import ru.viktorgezz.NauJava.security.util.CookieUtil;

/**
 * Контроллер (Thymeleaf) для регистрации и входа пользователей.
 */
@Controller
@RequestMapping("/ui/auth")
public class AuthThymeleafController {

    private final AuthenticationService authenticationService;
    private final JwtProperties jwtProperties;

    @Autowired
    public AuthThymeleafController(
            AuthenticationService authenticationService,
            JwtProperties jwtProperties
    ) {
        this.authenticationService = authenticationService;
        this.jwtProperties = jwtProperties;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("username") @NotBlank String username,
            @RequestParam @NotBlank String password,
            @RequestParam("confirmPassword") @NotBlank String confirmPassword
    ) {
        RegistrationRequest rq = new RegistrationRequest(username, password, confirmPassword);
        authenticationService.register(rq);
        return "redirect:/ui/auth/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String password,
            HttpServletResponse response
    ) {
        AuthenticationResponse auth = authenticationService.login(new AuthenticationRequest(name, password));

        Cookie access = CookieUtil.createCookieForJwtToken(
                auth.accessToken(),
                "accessToken",
                jwtProperties.getAccessExpirationSec()
        );
        Cookie refresh = CookieUtil.createCookieForJwtToken(
                auth.refreshToken(),
                "refreshToken",
                jwtProperties.getRefreshExpirationSec()
        );

        response.addCookie(access);
        response.addCookie(refresh);

        return "redirect:/ui/tests";
    }

    @PostMapping("/logout")
    public String handleLogout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            authenticationService.logout(refreshToken);
        }

        Cookie expiredAccess = CookieUtil.createCookieForJwtToken("", "accessToken", 0);
        Cookie expiredRefresh = CookieUtil.createCookieForJwtToken("", "refreshToken", 0);
        response.addCookie(expiredAccess);
        response.addCookie(expiredRefresh);

        return "redirect:/ui/auth/login";
    }
}
