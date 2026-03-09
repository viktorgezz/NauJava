/**
 * Утилиты для работы с хранением JWT токенов в localStorage.
 * Обеспечивает безопасное сохранение и получение access и refresh токенов.
 */
const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const TOKEN_TYPE_KEY = 'token_type'

export const tokenStorage = {
  /**
   * Сохраняет токены в localStorage
   * @param {string} accessToken - Access токен
   * @param {string} refreshToken - Refresh токен
   * @param {string} tokenType - Тип токена (обычно "Bearer")
   */
  setTokens(accessToken, refreshToken, tokenType = 'Bearer') {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
    localStorage.setItem(TOKEN_TYPE_KEY, tokenType)
  },

  /**
   * Получает access токен
   * @returns {string|null}
   */
  getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
  },

  /**
   * Получает refresh токен
   * @returns {string|null}
   */
  getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },

  /**
   * Получает тип токена
   * @returns {string|null}
   */
  getTokenType() {
    return localStorage.getItem(TOKEN_TYPE_KEY)
  },

  /**
   * Удаляет все токены из localStorage
   */
  clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(TOKEN_TYPE_KEY)
  },

  /**
   * Проверяет наличие токенов
   * @returns {boolean}
   */
  hasTokens() {
    return !!this.getAccessToken() && !!this.getRefreshToken()
  },
}
