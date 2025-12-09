/**
 * Pinia store для управления состоянием аутентификации.
 * Отвечает за хранение токенов, выполнение операций входа/выхода/регистрации
 * и автоматическое обновление токенов.
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authService from '@/api/authService'
import { tokenStorage } from '@/utils/tokenStorage'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(tokenStorage.getAccessToken())
  const refreshTokenValue = ref(tokenStorage.getRefreshToken())
  const tokenType = ref(tokenStorage.getTokenType() || 'Bearer')

  const isAuthenticated = computed(() => !!accessToken.value)

  /**
   * Устанавливает токены в store и localStorage
   */
  const setTokens = (newAccessToken, newRefreshToken, newTokenType = 'Bearer') => {
    accessToken.value = newAccessToken
    refreshTokenValue.value = newRefreshToken
    tokenType.value = newTokenType
    tokenStorage.setTokens(newAccessToken, newRefreshToken, newTokenType)
  }

  /**
   * Выполняет вход в систему
   */
  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials)
      setTokens(response.access_token, response.refresh_token, response.token_type || 'Bearer')
      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Ошибка входа в систему',
      }
    }
  }

  /**
   * Выполняет регистрацию нового пользователя
   */
  const register = async (userData) => {
    try {
      await authService.register(userData)
      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || 'Ошибка регистрации',
      }
    }
  }

  /**
   * Обновляет access токен используя refresh токен
   */
  const refreshAccessToken = async () => {
    if (!refreshTokenValue.value) {
      throw new Error('Refresh token отсутствует')
    }

    try {
      const response = await authService.refreshToken(refreshTokenValue.value)
      setTokens(
        response.access_token,
        response.refresh_token || refreshTokenValue.value,
        response.token_type || tokenType.value,
      )
      return response
    } catch (error) {
      logout()
      throw error
    }
  }

  /**
   * Выполняет выход из системы
   */
  const logout = async () => {
    try {
      if (refreshTokenValue.value) {
        await authService.logout(refreshTokenValue.value)
      }
    } catch (error) {
      console.error('Ошибка при выходе:', error)
    } finally {
      accessToken.value = null
      refreshTokenValue.value = null
      tokenType.value = null
      tokenStorage.clearTokens()
      router.push('/login')
    }
  }

  /**
   * Инициализирует store из localStorage при загрузке приложения
   */
  const initialize = () => {
    if (tokenStorage.hasTokens()) {
      accessToken.value = tokenStorage.getAccessToken()
      refreshTokenValue.value = tokenStorage.getRefreshToken()
      tokenType.value = tokenStorage.getTokenType() || 'Bearer'
    }
  }

  return {
    // Computed свойства для использования в компонентах
    accessToken: computed(() => accessToken.value),
    refreshToken: computed(() => refreshTokenValue.value),
    tokenType: computed(() => tokenType.value),
    isAuthenticated,
    // Геттеры для прямого доступа к значениям (для использования в интерцепторах)
    getAccessToken: () => accessToken.value,
    getRefreshToken: () => refreshTokenValue.value,
    login,
    register,
    refreshAccessToken,
    logout,
    initialize,
  }
})
