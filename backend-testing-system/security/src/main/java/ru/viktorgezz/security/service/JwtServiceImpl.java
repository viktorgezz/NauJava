package ru.viktorgezz.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.security.JwtProperties;
import ru.viktorgezz.security.RefreshToken;
import ru.viktorgezz.security.RefreshTokenRepo;
import ru.viktorgezz.security.exception.InvalidJwtTokenException;
import ru.viktorgezz.security.exception.TokenExpiredException;
import ru.viktorgezz.security.util.KeyUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

/**
 * Сервис работы с JWT-токенами. Реализует {@link JwtService}.
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ROLE = "role";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    private final RefreshTokenRepo refreshTokenRepo;


    @Autowired
    public JwtServiceImpl(
            RefreshTokenRepo refreshTokenRepo,
            JwtProperties jwtProperties
    ) throws Exception {
        this.refreshTokenRepo = refreshTokenRepo;
        this.privateKey = KeyUtils.loadPrivateKey("keys/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/public_key.pem");
        this.accessTokenExpiration = jwtProperties.getAccessExpirationMs();
        this.refreshTokenExpiration = jwtProperties.getRefreshExpirationMs();
    }

    public String generateAccessToken(final UserDetails userDetails) {
        final String role = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .replace("Role_", "");

        final String username = userDetails.getUsername();

        log.debug("Generate acc-token for {} with role: {}", username, role);
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                ROLE, role
        );
        return buildToken(username, claims, accessTokenExpiration);
    }

    private String generateAccessToken(final String username, final String role) {
        final Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                ROLE, role
        );

        return buildToken(username, claims, accessTokenExpiration);
    }

    @Transactional
    public String generateRefreshToken(final UserDetails userDetails) {
        final String username = userDetails.getUsername();

        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        final String refreshToken = buildToken(username, claims, refreshTokenExpiration);
        RefreshToken token = new RefreshToken(
                username,
                refreshToken,
                new Date(System.currentTimeMillis() + refreshTokenExpiration)
        );

        refreshTokenRepo.save(token);
        return refreshToken;
    }

    public boolean validateToken(final String token, final String usernameExpected) {
        final String username = extractUsername(token);
        return username.equals(usernameExpected) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaimsStrict(token).getSubject();
    }

    public String refreshToken(final String refreshToken) {
        final Claims claims = extractClaimsAllowExpired(refreshToken);
        final String username = claims.getSubject();
        final String role = claims.get(ROLE, String.class);

        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new InvalidJwtTokenException("Invalid refresh token");
        } else if (isExpired(claims) || isRefreshTokenWithdrown(refreshToken, username)) {
            throw new TokenExpiredException("Refresh token expired");
        }

        return generateAccessToken(username, role);
    }

    @Transactional
    public void dropRefreshToken(final String refreshToken) {
        refreshTokenRepo.deleteByToken(refreshToken);
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
        return isExpired(extractClaimsStrict(token));
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date(System.currentTimeMillis()));
    }

    private Claims extractClaimsAllowExpired(String token) {
        try {
            return extractClaims(token);
        } catch (final ExpiredJwtException e) {
            return e.getClaims();
        } catch (final JwtException e) {
            throw new InvalidJwtTokenException(e.getMessage());
        }
    }

    private Claims extractClaimsStrict(String token) {
        try {
            return extractClaims(token);
        } catch (final ExpiredJwtException e) {
            throw new TokenExpiredException("Invalid access token");
        } catch (final JwtException e) {
            throw new InvalidJwtTokenException(e.getMessage());
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isRefreshTokenWithdrown(String refreshToken, String username) {
        if (refreshToken == null) {
            return true;
        }

        boolean tokenExists = refreshTokenRepo
                .findRefreshTokensByUsername(username)
                .stream()
                .map(RefreshToken::getToken)
                .toList()
                .contains(refreshToken);

        return !tokenExists;
    }
}
