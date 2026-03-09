package ru.viktorgezz.testing_system.auth.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.viktorgezz.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.security.exception.TokenExpiredException;
import ru.viktorgezz.security.service.JwtService;
import ru.viktorgezz.testing_system.auth.dto.AuthenticationRequest;
import ru.viktorgezz.testing_system.auth.dto.AuthenticationResponse;
import ru.viktorgezz.testing_system.auth.dto.RefreshRequest;
import ru.viktorgezz.testing_system.auth.dto.RegistrationRequest;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.User;
import ru.viktorgezz.testing_system.domain.user.UserMapper;
import ru.viktorgezz.testing_system.domain.user.service.intrf.UserCommandService;
import ru.viktorgezz.testing_system.exception.BusinessException;
import ru.viktorgezz.testing_system.exception.ErrorCode;

/**
 * Сервис аутентификации пользователей. Реализует {@link AuthenticationService}.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserCommandService userCommandService;

    @Override
    public AuthenticationResponse login(AuthenticationRequest authRq) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRq.name(),
                        authRq.password()
                )
        );

        final User user = (User) authentication.getPrincipal();
        final String accessToken = jwtService.generateAccessToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);
        final String tokenType = "Bearer";

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                tokenType
        );
    }

    @Override
    public void register(RegistrationRequest request, Role role) {
        checkPasswords(request.password(), request.confirmPassword());
        final User user = UserMapper.toUser(request, role);
        user.setPassword(passwordEncoder.encode(request.password()));
        userCommandService.save(user);
        log.debug("Registering user: {}", user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        try {
            final String accessNewToken = jwtService.refreshToken(request.refreshToken());
            final String tokenType = "Bearer";

            return new AuthenticationResponse(
                    accessNewToken,
                    request.refreshToken(),
                    tokenType
            );
        } catch (InvalidJwtTokenException | TokenExpiredException e) {
            log.debug("Refresh token expired/invalid: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_REFRESH_EXPIRED);
        }
    }

    @Override
    public void logout(String refreshToken) {
        jwtService.dropRefreshToken(refreshToken);
    }

    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
    }
}
