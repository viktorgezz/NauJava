/**
 * Конфигурация HTTP клиента для работы с бэкенд API.
 * Настраивает базовый URL, заголовки и интерцепторы для автоматической работы с JWT токенами.
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_BACKEND_API_URL || 'http://localhost:8080'

// Один общий promise, чтобы не дергать /auth/refresh параллельно для каждого 401
let refreshPromise = null
// Флаг, что refresh уже провалился — чтобы не слать повторные запросы с истекшим токеном
let refreshFailed = false

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Интерцептор для добавления токена к запросам
apiClient.interceptors.request.use(
  (config) => {
    // Не добавляем токен для публичных эндпоинтов
    if (
      config.url?.includes('/auth/login') ||
      config.url?.includes('/auth/register') ||
      config.url?.includes('/auth/refresh')
    ) {
      return config
    }

    const authStore = useAuthStore()
    const token = authStore.getAccessToken()

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Интерцептор для обработки ошибок и обновления токенов
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Если получили 401 и это не запрос на обновление токена или публичный эндпоинт
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh') &&
      !originalRequest.url?.includes('/auth/login') &&
      !originalRequest.url?.includes('/auth/register')
    ) {
      originalRequest._retry = true

      // Если уже знаем, что refresh недоступен, просто выходим
      if (refreshFailed) {
        const authStore = useAuthStore()
        authStore.logout()
        return Promise.reject(error)
      }

      try {
        const authStore = useAuthStore()

        // Обеспечиваем один вызов /auth/refresh для пачки запросов с 401
        if (!refreshPromise) {
          refreshPromise = authStore.refreshAccessToken().finally(() => {
            refreshPromise = null
          })
        }

        await refreshPromise

        // Если refresh успешен, сбрасываем флаг ошибки
        refreshFailed = false

        // Повторяем оригинальный запрос с новым токеном
        originalRequest.headers.Authorization = `Bearer ${authStore.getAccessToken()}`
        return apiClient(originalRequest)
      } catch (refreshError) {
        // Если refresh не удался (например, истек refresh token), только тогда разлогиниваемся
        const authStore = useAuthStore()
        refreshFailed = true
        
        // Проверяем, что это действительно ошибка refresh token, а не другая ошибка
        const isRefreshTokenError = 
          refreshError.response?.status === 401 ||
          refreshError.message?.includes('refresh') ||
          !authStore.getRefreshToken()
        
        if (isRefreshTokenError) {
          authStore.logout()
        }
        
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export default apiClient
