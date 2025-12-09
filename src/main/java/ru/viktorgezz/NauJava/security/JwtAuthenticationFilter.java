package ru.viktorgezz.NauJava.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import ru.viktorgezz.NauJava.exception.security.InvalidJwtTokenException;
import ru.viktorgezz.NauJava.exception.security.TokenExpiredException;
import ru.viktorgezz.NauJava.security.service.JwtService;

import java.io.IOException;

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

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String tokenAccess = getAccessToken(request);

            if (tokenAccess == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.extractUsername(tokenAccess);

            if (username != null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(tokenAccess, userDetails.getUsername())) {
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
        } catch (TokenExpiredException | InvalidJwtTokenException e) {
            log.debug("{}", e.getMessage());
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
