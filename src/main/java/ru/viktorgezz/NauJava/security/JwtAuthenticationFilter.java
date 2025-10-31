package ru.viktorgezz.NauJava.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.viktorgezz.NauJava.security.service.JwtService;
import ru.viktorgezz.NauJava.security.util.CookieUtil;

import java.io.IOException;
import java.util.Arrays;

/**
 * Фильтр для аутентификации по JWT.
 * <p>
 * Порядок извлечения токена:
 * 1) Заголовок {@code Authorization: Bearer <token>}
 * 2) Cookie {@code accessToken}
 * 3) Попытка обновления по Cookie {@code refreshToken} с установкой нового access-токена в cookie
 * <p>
 * При успешной валидации формирует {@link UsernamePasswordAuthenticationToken}
 * и записывает его в {@link SecurityContextHolder}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            JwtProperties jwtProperties
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = getTokenFromRequest(request, response);

        if (jwt == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User '{}' authenticated successfully.", username);
                }
            }
        } catch (Exception e) {
            log.warn("Invalid JWT Token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String accessCookie = getCookieValue(request, "accessToken");
        if (accessCookie != null) {
            try {
                jwtService.extractUsername(accessCookie);
                return accessCookie;
            } catch (Exception ex) {
                log.debug("Access token from cookie expired, trying to refresh.");
                return tryRefresh(request, response);
            }
        }

        return tryRefresh(request, response);
    }

    private String tryRefresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refresh = getCookieValue(request, "refreshToken");
            if (refresh == null) {
                return null;
            }
            String newAccess = jwtService.refreshToken(refresh);

            Cookie access = CookieUtil.createCookieForJwtToken(
                    newAccess, "accessToken", jwtProperties.getAccessExpirationSec()
            );
            response.addCookie(access);
            log.debug("Refreshed access token via cookie.");
            return newAccess;
        } catch (Exception e) {
            log.debug("Refresh via cookie failed: {}", e.getMessage());
            return null;
        }
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
