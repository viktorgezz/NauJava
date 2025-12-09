package ru.viktorgezz.NauJava.security.service;

/**
 * Сервис для управления JSON Web Tokens (JWT).
 * Отвечает за создание, валидацию, обновление и извлечение данных
 * из access и refresh токенов.
 */
public interface JwtService {

    /**
     * Генерирует Access Token для указанного пользователя.
     *
     * @param username Имя пользователя (subject) токена.
     * @return Строка с Access Token.
     */
    String generateAccessToken(String username);

    /**
     * Генерирует и сохраняет Refresh Token для указанного пользователя.
     *
     * @param username Имя пользователя (subject) токена.
     * @return Строка с Refresh Token.
     */
    String generateRefreshToken(String username);

    /**
     * Проверяет, является ли токен действительным (не просрочен) и
     * принадлежит ли он ожидаемому пользователю.
     *
     * @param token Токен для проверки.
     * @param usernameExpected Ожидаемое имя пользователя.
     * @return {@code true}, если токен валиден, иначе {@code false}.
     */
    boolean validateToken(String token, String usernameExpected);

    /**
     * Извлекает имя пользователя (subject) из токена.
     *
     * @param token Токен, из которого извлекаются данные.
     * @return Имя пользователя.
     */
    String extractUsername(String token);

    /**
     * Использует Refresh Token для генерации нового Access Token.
     *
     * @param refreshToken Действительный Refresh Token.
     * @return Новый Access Token.
     */
    String refreshToken(String refreshToken);

    /**
     * Удаляет (аннулирует) указанный Refresh Token.
     *
     * @param refreshToken Refresh Token, который будет удален.
     */
    void dropRefreshToken(String refreshToken);
}