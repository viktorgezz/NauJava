package ru.viktorgezz.NauJava.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.security.util.KeyUtils;
import ru.viktorgezz.NauJava.security.RefreshToken;
import ru.viktorgezz.NauJava.security.RefreshTokenRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.service.intrf.UserQueryService;
import ru.viktorgezz.NauJava.security.JwtProperties;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

/**
 * Сервис работы с JWT-токенами. Реализует {@link JwtService}.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ROLE = "role";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private final UserQueryService userQueryService;
    private final RefreshTokenRepo refreshTokenRepo;


    @Autowired
    public JwtServiceImpl(
            UserQueryService userQueryService,
            RefreshTokenRepo refreshTokenRepo,
            JwtProperties jwtProperties
    ) throws Exception {
        this.userQueryService = userQueryService;
        this.refreshTokenRepo = refreshTokenRepo;
        this.privateKey = KeyUtils.loadPrivateKey("keys/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/public_key.pem");
        this.accessTokenExpiration = jwtProperties.getAccessExpirationMs();
        this.refreshTokenExpiration = jwtProperties.getRefreshExpirationMs();
    }

    public String generateAccessToken(final String username) {
        final User userSaved = userQueryService.getByUsername(username);
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                ROLE, userSaved.getRole().name()
        );
        return buildToken(username, claims, accessTokenExpiration);
    }

    @Transactional
    public String generateRefreshToken(final String username) {

        final User userFound = userQueryService.getByUsername(username);

        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        final String refreshToken = buildToken(username, claims, refreshTokenExpiration);
        RefreshToken token = new RefreshToken(
                userFound,
                refreshToken,
                new Date(System.currentTimeMillis() + refreshTokenExpiration)
        );


        userFound.getRefreshTokens().add(token);
        refreshTokenRepo.save(token);
        return refreshToken;
    }

    public boolean validateToken(final String token, final String usernameExpected) {
        final String username = extractUsername(token);
        return username.equals(usernameExpected) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String refreshToken(final String refreshToken) {
        final Claims claims = extractClaims(refreshToken);
        final String username = claims.getSubject();
        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new RuntimeException("Invalid refresh token");
        }
        if (isTokenExpired(refreshToken) || isRefreshTokenWithdrown(refreshToken, username)) {
            throw new RuntimeException("Refresh token expired");
        }
        return generateAccessToken(username);
    }

    @Transactional
    public void dropRefreshToken(final String refreshToken) {
        refreshTokenRepo.deleteByRefreshToken(refreshToken);
    }

    private String buildToken(
            String username,
            Map<String, Object> claims,
            long expiration
    ) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration()
                .before(new Date());
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final JwtException ex) {
            throw new RuntimeException("Invalid JWT token", ex);
        }
    }

    private boolean isRefreshTokenWithdrown(String refreshToken, String username) {
        if (refreshToken == null) {
            return false;
        }
        return refreshTokenRepo
                .findRefreshTokensByUsername(username)
                .stream()
                .map(RefreshToken::getRefreshToken)
                .toList()
                .contains(refreshToken);
    }
}
