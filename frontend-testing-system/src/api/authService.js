/**
 * Сервис для работы с API аутентификации.
 * Инкапсулирует все запросы связанные с входом, регистрацией и обновлением токенов.
 */
import apiClient from './config'

/**
 * Вход в систему
 * @param {Object} credentials - Данные для входа
 * @param {string} credentials.name - Имя пользователя
 * @param {string} credentials.password - Пароль
 * @returns {Promise<Object>} Ответ с токенами
 */
export const login = async (credentials) => {
  const response = await apiClient.post('/auth/login', credentials)
  return response.data
}

/**
 * Регистрация нового пользователя
 * @param {Object} userData - Данные для регистрации
 * @param {string} userData.username - Имя пользователя
 * @param {string} userData.password - Пароль
 * @param {string} userData.confirmPassword - Подтверждение пароля
 * @returns {Promise<void>}
 */
export const register = async (userData) => {
  await apiClient.post('/auth/register', userData)
}

/**
 * Обновление access токена по refresh токену
 * @param {string} refreshToken - Refresh токен
 * @returns {Promise<Object>} Новые токены
 */
export const refreshToken = async (refreshToken) => {
  const response = await apiClient.post('/auth/refresh', { refreshToken })
  return response.data
}

/**
 * Выход из системы
 * @param {string} refreshToken - Refresh токен для инвалидации
 * @returns {Promise<void>}
 */
export const logout = async (refreshToken) => {
  await apiClient.get('/auth/logout', {
    params: { refreshToken: refreshToken || '' },
  })
}
