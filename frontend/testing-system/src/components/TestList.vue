<!--
  Компонент списка тестов с пагинацией и поиском.
  Отображает карточки тестов, реализует поиск по названию и навигацию по страницам.
-->
<template>
  <div class="test-list-container">
    <div class="test-list-header">
      <h1 class="test-list-title">Библиотека тестов</h1>
      <div class="search-container">
        <input
          v-model="searchTitle"
          type="text"
          class="search-input"
          placeholder="Поиск по названию..."
          @input="handleSearch"
        />
        <button v-if="searchTitle" @click="clearSearch" class="btn-clear-search" title="Очистить">
          ×
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Загрузка тестов...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p class="error-text">{{ error }}</p>
      <button @click="loadTests" class="btn btn-secondary">Попробовать снова</button>
    </div>

    <div v-else-if="tests.length === 0" class="empty-container">
      <p class="empty-text">Тесты не найдены</p>
    </div>

    <div v-else class="test-grid">
      <div v-for="test in tests" :key="test.id" class="test-card" @click="handleTestClick(test.id)">
        <div class="test-card-header">
          <h3 class="test-title" :title="test.title">{{ truncateText(test.title, 60) }}</h3>
          <span class="test-status" :class="getStatusClass(test.status)">
            {{ getStatusLabel(test.status) }}
          </span>
        </div>

        <p v-if="test.description" class="test-description" :title="test.description">
          {{ truncateText(test.description, 150) }}
        </p>
        <p v-else class="test-description text-muted">Описание отсутствует</p>

        <div class="test-meta">
          <div class="test-author">
            <span class="meta-label">Автор:</span>
            <span class="meta-value">{{ test.author?.username || 'Неизвестен' }}</span>
          </div>

          <div v-if="test.namesTopics && test.namesTopics.length > 0" class="test-topics">
            <span class="meta-label">Темы:</span>
            <div class="topics-list">
              <span v-for="topic in test.namesTopics" :key="topic" class="topic-tag">
                {{ topic }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <TestDetailsModal
      :is-open="isDetailsModalOpen"
      :test-id="selectedTestId"
      @close="closeDetailsModal"
      @edit="handleEditTest"
      @metadata-updated="handleMetadataUpdated"
      @test-deleted="handleTestDeleted"
    />

    <div v-if="!loading && tests.length > 0" class="pagination-container">
      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 0"
        class="btn btn-pagination"
      >
        Назад
      </button>

      <div class="pagination-info">
        <span>Страница {{ currentPage + 1 }} из {{ Math.max(totalPages, 1) }}</span>
        <span class="pagination-total">Всего тестов: {{ totalElements }}</span>
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
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as testService from '@/api/testService'
import TestDetailsModal from '@/components/TestDetailsModal.vue'

const router = useRouter()

const tests = ref([])
const loading = ref(false)
const error = ref(null)
const searchTitle = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 20

const isDetailsModalOpen = ref(false)
const selectedTestId = ref(null)

let searchTimeout = null

/**
 * Загружает список тестов
 */
const loadTests = async () => {
  loading.value = true
  error.value = null

  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
    }

    let response
    if (searchTitle.value.trim()) {
      response = await testService.getTestsByTitle(searchTitle.value.trim(), params)
    } else {
      response = await testService.getTests(params)
    }

    tests.value = response.content || []
    totalPages.value = response.totalPages ?? 1
    totalElements.value = response.totalElements ?? 0
  } catch (err) {
    error.value = err.response?.data?.message || 'Ошибка при загрузке тестов'
    tests.value = []
  } finally {
    loading.value = false
  }
}

/**
 * Публичный метод для обновления списка тестов извне
 */
defineExpose({
  refresh: loadTests,
})

/**
 * Обработка поиска с задержкой
 */
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadTests()
  }, 500)
}

/**
 * Очистка поиска
 */
const clearSearch = () => {
  searchTitle.value = ''
  currentPage.value = 0
  loadTests()
}

/**
 * Переход на страницу
 */
const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadTests()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

/**
 * Обработка клика по тесту
 * @param {number} testId - ID теста
 */
const handleTestClick = (testId) => {
  selectedTestId.value = testId
  isDetailsModalOpen.value = true
}

/**
 * Закрытие модального окна с деталями теста
 */
const closeDetailsModal = () => {
  isDetailsModalOpen.value = false
  selectedTestId.value = null
}

/**
 * Обработка нажатия на кнопку "Изменить содержимое теста"
 * @param {number} testId - ID теста
 */
const handleEditTest = (testId) => {
  router.push(`/tests/${testId}/edit`)
}

/**
 * Обработка обновления метаданных теста
 */
const handleMetadataUpdated = () => {
  loadTests()
}

/**
 * Обработка удаления теста
 */
const handleTestDeleted = () => {
  loadTests()
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
 * Обрезает текст до указанной длины и добавляет многоточие
 * @param {string} text - Текст для обрезки
 * @param {number} maxLength - Максимальная длина
 * @returns {string} Обрезанный текст с многоточием
 */
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength).trim() + '...'
}

onMounted(() => {
  loadTests()
})

onBeforeUnmount(() => {
  // Очистка таймера при размонтировании компонента
  if (searchTimeout) {
    clearTimeout(searchTimeout)
    searchTimeout = null
  }
})

watch(currentPage, () => {
  loadTests()
})
</script>

<style scoped>
.test-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.test-list-header {
  margin-bottom: 30px;
}

.test-list-title {
  color: #00ff88;
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 24px;
}

.search-container {
  position: relative;
  max-width: 500px;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 16px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.search-input::placeholder {
  color: #666;
}

.btn-clear-search {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: #999;
  font-size: 24px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.btn-clear-search:hover {
  background: #333;
  color: #e0e0e0;
}

.loading-container,
.error-container,
.empty-container {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #333;
  border-top-color: #00ff88;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text,
.error-text,
.empty-text {
  color: #999;
  font-size: 18px;
}

.error-text {
  color: #ff4444;
  margin-bottom: 16px;
}

.test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.test-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.test-card:hover {
  border-color: #00ff88;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 255, 136, 0.2);
}

.test-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

.test-title {
  color: #00ff88;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.test-status {
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

.test-description {
  color: #e0e0e0;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
  min-height: 40px;
  overflow: hidden;
  word-wrap: break-word;
}

.test-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #333;
}

.test-author,
.test-topics {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
}

.meta-label {
  color: #999;
  font-weight: 500;
  flex-shrink: 0;
}

.meta-value {
  color: #e0e0e0;
}

.topics-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.topic-tag {
  padding: 4px 10px;
  background: rgba(0, 255, 136, 0.1);
  color: #00ff88;
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.text-muted {
  color: #666;
  font-style: italic;
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

.btn-secondary {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
}

.btn-secondary:hover {
  background: #00ff88;
  color: #0a0a0a;
}

@media (max-width: 768px) {
  .test-grid {
    grid-template-columns: 1fr;
  }

  .pagination-container {
    flex-direction: column;
  }

  .pagination-info {
    order: -1;
  }
}
</style>
