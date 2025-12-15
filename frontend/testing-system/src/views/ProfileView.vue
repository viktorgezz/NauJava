<!--
  Страница профиля пользователя.
  Отображает информацию о пользователе, список результатов тестов с пагинацией.
-->
<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="header-content">
        <h1 class="profile-title">Профиль</h1>
        <div class="header-actions">
          <button @click="goToHome" class="btn btn-secondary">На главную</button>
          <button @click="handleLogout" class="btn btn-logout">Выйти</button>
        </div>
      </div>
    </div>

    <div v-if="userLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Загрузка профиля...</p>
    </div>

    <div v-else-if="userError" class="error-container">
      <p class="error-text">{{ userError }}</p>
      <button @click="loadUser" class="btn btn-secondary">Попробовать снова</button>
    </div>

    <div v-else-if="user" class="profile-content">
      <!-- Информация о пользователе -->
      <div class="user-info-card">
        <div class="user-avatar">
          <span class="avatar-icon">👤</span>
        </div>
        <div class="user-details">
          <h2 class="user-name">{{ user.username }}</h2>
          <div class="user-meta">
            <span v-if="user.role" class="meta-item">
              <span class="meta-label">Роль:</span>
              <span class="meta-value">{{ user.role }}</span>
            </span>
          </div>
        </div>
      </div>

      <!-- Список результатов -->
      <div class="results-section">
        <h2 class="section-title">Мои результаты</h2>

        <div v-if="resultsLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">Загрузка результатов...</p>
        </div>

        <div v-else-if="resultsError" class="error-container">
          <p class="error-text">{{ resultsError }}</p>
          <button @click="loadResults" class="btn btn-secondary">Попробовать снова</button>
        </div>

        <div v-else-if="results.length === 0" class="empty-container">
          <p class="empty-text">У вас пока нет результатов прохождения тестов</p>
        </div>

        <div v-else class="results-list">
          <div v-for="result in results" :key="result.id" class="result-card">
            <div class="result-card-header">
              <h3 class="result-test-title">{{ result.titleTest }}</h3>
              <span class="result-status" :class="getStatusClass(result.status)">
                {{ getStatusLabel(result.status) }}
              </span>
            </div>

            <div class="result-stats">
              <div class="stat-item">
                <span class="stat-label">Баллы:</span>
                <span class="stat-value"
                  >{{ formatScore(result.point) }} / {{ formatScore(result.pointMax) }}</span
                >
              </div>
              <div class="stat-item">
                <span class="stat-label">Процент:</span>
                <span class="stat-value" :class="getPercentClass(getPercent(result))"
                  >{{ getPercent(result) }}%</span
                >
              </div>
              <div class="stat-item">
                <span class="stat-label">Время:</span>
                <span class="stat-value">{{ formatTime(result.timeSpentSeconds) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Дата:</span>
                <span class="stat-value">{{ formatDate(result.completedAt) }}</span>
              </div>
            </div>

            <div class="result-actions">
              <button @click="viewResult(result)" class="btn btn-primary btn-sm">
                Просмотреть результат
              </button>
              <button @click="startTest(result.idTest)" class="btn btn-secondary btn-sm">
                Начать тест заново
              </button>
            </div>
          </div>
        </div>

        <!-- Пагинация -->
        <div v-if="!resultsLoading && results.length > 0" class="pagination-container">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="btn btn-pagination"
          >
            Назад
          </button>

          <div class="pagination-info">
            <span>Страница {{ currentPage + 1 }} из {{ Math.max(totalPages, 1) }}</span>
            <span class="pagination-total">Всего результатов: {{ totalElements }}</span>
          </div>

          <button
            @click="goToPage(currentPage + 1)"
            :disabled="totalPages === 0 || currentPage >= totalPages - 1"
            class="btn btn-pagination"
          >
            Вперед
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getCurrentUser } from '@/api/userService'
import { getUserResults } from '@/api/resultService'

const router = useRouter()
const authStore = useAuthStore()

const user = ref(null)
const userLoading = ref(false)
const userError = ref(null)

const results = ref([])
const resultsLoading = ref(false)
const resultsError = ref(null)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 20

/**
 * Загружает информацию о пользователе
 */
const loadUser = async () => {
  userLoading.value = true
  userError.value = null

  try {
    user.value = await getCurrentUser()
  } catch (err) {
    userError.value = err.response?.data?.message || 'Ошибка при загрузке профиля'
  } finally {
    userLoading.value = false
  }
}

/**
 * Загружает список результатов
 */
const loadResults = async () => {
  resultsLoading.value = true
  resultsError.value = null

  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      sort: 'completedAt,DESC',
    }

    const response = await getUserResults(params)
    results.value = response.content || []
    totalPages.value = response.totalPages ?? 1
    totalElements.value = response.totalElements ?? 0
  } catch (err) {
    resultsError.value = err.response?.data?.message || 'Ошибка при загрузке результатов'
    results.value = []
  } finally {
    resultsLoading.value = false
  }
}

/**
 * Переход на страницу
 */
const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadResults()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

/**
 * Просмотр результата
 */
const viewResult = (result) => {
  router.push(`/results/${result.id}`)
}

/**
 * Начать тест заново
 */
const startTest = (testId) => {
  router.push(`/tests/${testId}/pass`)
}

/**
 * Переход на главную страницу
 */
const goToHome = () => {
  router.push('/')
}

/**
 * Выход из системы
 */
const handleLogout = async () => {
  await authStore.logout()
}

/**
 * Форматирует балл
 */
const formatScore = (score) => {
  if (score === null || score === undefined) return '—'
  return Number(score).toFixed(2)
}

/**
 * Вычисляет процент
 */
const getPercent = (result) => {
  if (!result.pointMax || result.pointMax === 0) return 0
  return Math.round((result.point / result.pointMax) * 100)
}

/**
 * Форматирует время
 */
const formatTime = (seconds) => {
  if (!seconds) return '—'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  if (hours > 0) {
    return `${hours}ч ${minutes}м ${secs}с`
  }
  if (minutes > 0) {
    return `${minutes}м ${secs}с`
  }
  return `${secs}с`
}

/**
 * Форматирует дату
 */
const formatDate = (dateString) => {
  if (!dateString) return '—'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return dateString
  }
}

/**
 * Получает класс для статуса
 */
const getStatusClass = (status) => {
  const statusMap = {
    PUBLIC: 'status-public',
    UNLISTED: 'status-unlisted',
    PRIVATE: 'status-private',
  }
  return statusMap[status] || ''
}

/**
 * Получает метку для статуса
 */
const getStatusLabel = (status) => {
  const labelMap = {
    PUBLIC: 'Публичный',
    UNLISTED: 'По ссылке',
    PRIVATE: 'Приватный',
  }
  return labelMap[status] || status
}

/**
 * Получает класс для процента
 */
const getPercentClass = (percent) => {
  if (percent >= 95) return 'percent-excellent'
  if (percent >= 85) return 'percent-good'
  if (percent >= 70) return 'percent-satisfactory'
  return 'percent-fail'
}

onMounted(() => {
  loadUser()
  loadResults()
})

watch(currentPage, () => {
  loadResults()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  padding-bottom: 60px;
}

.profile-header {
  background: #1a1a1a;
  border-bottom: 2px solid #333;
  padding: 20px;
  margin-bottom: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.loading-container,
.error-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px;
  text-align: center;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 5px solid #333;
  border-top-color: #00ff88;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 24px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: #e0e0e0;
  font-size: 20px;
  margin: 0;
}

.error-text {
  color: #ff4444;
  font-size: 18px;
  margin-bottom: 24px;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.user-info-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(0, 255, 136, 0.1);
  border: 3px solid #00ff88;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 48px;
}

.user-details {
  flex: 1;
}

.user-name {
  color: #00ff88;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 16px;
}

.user-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.meta-label {
  color: #666;
  font-weight: 500;
}

.meta-value {
  color: #e0e0e0;
}

.results-section {
  margin-top: 32px;
}

.section-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 24px;
}

.empty-container {
  text-align: center;
  padding: 60px 20px;
}

.empty-text {
  color: #999;
  font-size: 18px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.result-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.result-card:hover {
  border-color: #00ff88;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 255, 136, 0.2);
}

.result-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #333;
}

.result-test-title {
  color: #00ff88;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.result-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-public {
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
  border: 1px solid #00ff88;
}

.status-unlisted {
  background: rgba(255, 170, 0, 0.2);
  color: #ffaa00;
  border: 1px solid #ffaa00;
}

.status-private {
  background: rgba(255, 68, 68, 0.2);
  color: #ff4444;
  border: 1px solid #ff4444;
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  color: #666;
  font-size: 13px;
  font-weight: 500;
}

.stat-value {
  color: #e0e0e0;
  font-size: 16px;
  font-weight: 600;
}

.percent-excellent {
  color: #00ff88;
}

.percent-good {
  color: #00aaff;
}

.percent-satisfactory {
  color: #ffaa00;
}

.percent-fail {
  color: #ff4444;
}

.result-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  flex-wrap: wrap;
}

.pagination-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #e0e0e0;
  font-size: 14px;
}

.pagination-total {
  color: #999;
  font-size: 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-sm {
  padding: 8px 16px;
  font-size: 13px;
}

.btn-logout {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
}

.btn-logout:hover {
  background: #00ff88;
  color: #0a0a0a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.btn-primary {
  background: #00ff88;
  color: #0a0a0a;
}

.btn-primary:hover {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.btn-secondary {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
}

.btn-secondary:hover {
  background: #00ff88;
  color: #0a0a0a;
}

.btn-pagination {
  background: #00ff88;
  color: #0a0a0a;
  min-width: 100px;
}

.btn-pagination:hover:not(:disabled) {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.btn-pagination:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }

  .result-card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-stats {
    grid-template-columns: 1fr;
  }

  .result-actions {
    flex-direction: column;
  }

  .result-actions .btn {
    width: 100%;
  }

  .pagination-container {
    flex-direction: column;
  }

  .pagination-info {
    order: -1;
  }
}
</style>
