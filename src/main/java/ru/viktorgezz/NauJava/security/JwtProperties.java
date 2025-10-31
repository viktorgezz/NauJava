package ru.viktorgezz.NauJava.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Свойства JWT, считываются из application.yml (security.jwt).
 */
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private long accessExpirationMs;

    private long refreshExpirationMs;

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public int getAccessExpirationSec() {
        return toSafeSecondsInt(accessExpirationMs);
    }

    public int getRefreshExpirationSec() {
        return toSafeSecondsInt(refreshExpirationMs);
    }

    private int toSafeSecondsInt(long millis) {
        if (millis <= 0L) {
            return 0;
        }
        long seconds = millis / 1000L;
        if (seconds > Integer.MAX_VALUE) {
            throw new ArithmeticException("Integer overflow. Cannot convert " + seconds + " seconds to int.");
        }
        return (int) seconds;
    }

    public void setAccessExpirationMs(long accessExpirationMs) {
        this.accessExpirationMs = accessExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
