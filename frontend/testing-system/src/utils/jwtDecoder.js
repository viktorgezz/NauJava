/**
 * Утилита для декодирования JWT токенов.
 * Извлекает данные из payload токена без проверки подписи.
 */

/**
 * Декодирует JWT токен и возвращает payload
 * @param {string} token - JWT токен
 * @returns {Object|null} Payload токена или null при ошибке
 */
export const decodeJWT = (token) => {
  if (!token) return null

  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null

    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch (error) {
    console.error('Ошибка декодирования JWT:', error)
    return null
  }
}

/**
 * Извлекает username из JWT токена
 * @param {string} token - JWT токен
 * @returns {string|null} Username или null
 */
export const getUsernameFromToken = (token) => {
  const payload = decodeJWT(token)
  if (!payload) return null

  // JWT токены обычно содержат 'sub' (subject) с username
  return payload.sub || payload.username || null
}
