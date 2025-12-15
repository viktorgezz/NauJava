/**
 * Сервис для работы с API пользователей.
 * Инкапсулирует запросы связанные с получением информации о пользователях.
 */
import apiClient from './config'

/**
 * Получает информацию о текущем пользователе
 * @returns {Promise<Object>} Данные пользователя (UserResponseDto)
 */
export const getCurrentUser = async () => {
  const response = await apiClient.get('/users/me')
  return response.data
}

