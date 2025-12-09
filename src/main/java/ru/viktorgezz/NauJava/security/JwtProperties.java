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

    public void setAccessExpirationMs(long accessExpirationMs) {
        this.accessExpirationMs = accessExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
