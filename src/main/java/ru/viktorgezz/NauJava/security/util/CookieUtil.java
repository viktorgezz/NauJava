package ru.viktorgezz.NauJava.security.util;

import jakarta.servlet.http.Cookie;

/**
 * Утилиты для работы с cookie токенов JWT.
 */
public class CookieUtil {

    private CookieUtil(){}

    public static Cookie createCookieForJwtToken(
            String token,
            String nameToken,
            long timeLive
            ) {
        Cookie cookie = new Cookie(nameToken, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) timeLive);
        return cookie;
    }
}
