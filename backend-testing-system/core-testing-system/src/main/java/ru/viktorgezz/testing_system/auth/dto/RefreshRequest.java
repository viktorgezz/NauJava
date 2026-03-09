package ru.viktorgezz.testing_system.auth.dto;

/**
 * Запрос на обновление access-токена по refresh-токену.
 *
 * @param refreshToken действующий refresh-токен
 */
public record RefreshRequest(
        String refreshToken
) {
}
