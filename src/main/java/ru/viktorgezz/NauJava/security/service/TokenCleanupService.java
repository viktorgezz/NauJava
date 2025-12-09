package ru.viktorgezz.NauJava.security.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.security.RefreshTokenRepo;

import java.util.Date;

/**
 * Сервис для периодической очистки истекших refresh токенов из базы данных.
 */
@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final RefreshTokenRepo refreshTokenRepo;

    @Autowired
    public TokenCleanupService(RefreshTokenRepo refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Scheduled(cron = "0 30 12 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        try {
            Date now = new Date();
            refreshTokenRepo.deleteExpiredTokens(now);
            log.debug("Purged expired tokens in {}", now);
        } catch (Exception e) {
            log.error("Error purging expired tokens {}", e.getMessage(), e);
        }
    }
}
