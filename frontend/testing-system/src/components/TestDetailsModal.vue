<!--
  Модальное окно для отображения детальной информации о тесте.
  Реализует glassmorphism эффект и отображает полную информацию о тесте.
-->
<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="handleClose">
    <div class="modal-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">Загрузка информации о тесте...</p>
      </div>

      <div v-else-if="error" class="error-container">
        <p class="error-text">{{ error }}</p>
        <button @click="handleClose" class="btn btn-secondary">Закрыть</button>
      </div>

      <div v-else-if="testData" class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title">{{ testData.title }}</h2>
          <button @click="handleClose" class="btn-close" aria-label="Закрыть">×</button>
        </div>

        <div class="modal-body">
          <div class="info-section">
            <div class="info-item">
              <span class="info-label">Описание:</span>
              <p class="info-value">{{ testData.description || 'Описание отсутствует' }}</p>
            </div>

            <div class="info-item">
              <span class="info-label">Статус:</span>
              <span class="status-badge" :class="getStatusClass(testData.status)">
                {{ getStatusLabel(testData.status) }}
              </span>
            </div>

            <div class="info-item">
              <span class="info-label">Автор:</span>
              <span class="info-value">{{ testData.author?.username || 'Неизвестен' }}</span>
            </div>

            <div v-if="testData.namesTopics && testData.namesTopics.length > 0" class="info-item">
              <span class="info-label">Темы:</span>
              <div class="topics-list">
                <span v-for="topic in testData.namesTopics" :key="topic" class="topic-tag">
                  {{ topic }}
                </span>
              </div>
            </div>
            <div v-else class="info-item">
              <span class="info-label">Темы:</span>
              <span class="info-value text-muted">Темы не указаны</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button v-if="isTestOwner" @click="handleEditMetadata" class="btn btn-primary">
            Изменить описание
          </button>
          <button v-if="isTestOwner" @click="handleEdit" class="btn btn-primary">
            Изменить содержимое теста
          </button>
          <button @click="handleTakeTest" class="btn btn-secondary">Пройти тест</button>
          <button @click="handleClose" class="btn btn-close-modal">Закрыть</button>
        </div>
      </div>
    </div>

    <EditTestMetadataModal
      :is-open="isEditMetadataModalOpen"
      :test-id="props.testId"
      @close="isEditMetadataModalOpen = false"
      @success="handleMetadataUpdated"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getTestById } from '@/api/testService'
import { useAuthStore } from '@/stores/auth'
import { getUsernameFromToken } from '@/utils/jwtDecoder'
import EditTestMetadataModal from '@/components/EditTestMetadataModal.vue'

const router = useRouter()

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  testId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['close', 'edit', 'metadata-updated'])

const authStore = useAuthStore()
const testData = ref(null)
const loading = ref(false)
const error = ref(null)
const isEditMetadataModalOpen = ref(false)

/**
 * Проверяет, является ли текущий пользователь владельцем теста
 */
const isTestOwner = computed(() => {
  if (!testData.value || !testData.value.author) return false

  const token = authStore.getAccessToken()
  const currentUsername = getUsernameFromToken(token)

  return currentUsername === testData.value.author.username
})

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
 * Загружает информацию о тесте
 */
const loadTestData = async () => {
  if (!props.testId) return

  loading.value = true
  error.value = null
  testData.value = null

  try {
    testData.value = await getTestById(props.testId)
  } catch (err) {
    error.value = err.response?.data?.message || 'Ошибка при загрузке информации о тесте'
  } finally {
    loading.value = false
  }
}

/**
 * Обработка закрытия модального окна
 */
const handleClose = () => {
  testData.value = null
  error.value = null
  emit('close')
}

/**
 * Обработка нажатия на кнопку "Изменить описание"
 */
const handleEditMetadata = () => {
  isEditMetadataModalOpen.value = true
}

/**
 * Обработка успешного обновления метаданных
 */
const handleMetadataUpdated = () => {
  isEditMetadataModalOpen.value = false
  // Перезагружаем данные теста
  loadTestData()
  // Эмитируем событие для обновления списка тестов на главной странице
  emit('metadata-updated')
}

/**
 * Обработка нажатия на кнопку "Изменить содержимое теста"
 */
const handleEdit = () => {
  handleClose()
  router.push(`/tests/${props.testId}/edit`)
}

/**
 * Обработка нажатия на кнопку "Пройти тест"
 */
const handleTakeTest = () => {
  handleClose()
  router.push(`/tests/${props.testId}/pass`)
}

// Загружаем данные при открытии модального окна
watch(
  () => [props.isOpen, props.testId],
  ([newIsOpen, newTestId]) => {
    if (newIsOpen && newTestId) {
      loadTestData()
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: rgba(26, 26, 26, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 16px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-sizing: border-box;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(0, 255, 136, 0.1) inset,
    0 8px 32px rgba(0, 255, 136, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 30px;
  border-bottom: 1px solid rgba(0, 255, 136, 0.2);
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.modal-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  flex: 1;
  min-width: 0;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
}

.btn-close {
  background: transparent;
  border: none;
  color: #999;
  font-size: 32px;
  line-height: 1;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: 16px;
}

.btn-close:hover {
  background: #333;
  color: #e0e0e0;
}

.modal-body {
  padding: 30px;
  overflow-wrap: break-word;
  word-wrap: break-word;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.info-label {
  color: #00ff88;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #e0e0e0;
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
  max-width: 100%;
}

.text-muted {
  color: #999;
  font-style: italic;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  display: inline-block;
  width: fit-content;
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

.topics-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-tag {
  padding: 6px 12px;
  background: rgba(0, 255, 136, 0.1);
  color: #00ff88;
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 30px;
  border-top: 1px solid rgba(0, 255, 136, 0.2);
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
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

.btn-close-modal {
  background: transparent;
  color: #999;
  border: 2px solid #333;
}

.btn-close-modal:hover {
  background: #333;
  color: #e0e0e0;
  border-color: #666;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 30px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #333;
  border-top-color: #00ff88;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text,
.error-text {
  color: #999;
  font-size: 16px;
}

.error-text {
  color: #ff4444;
  margin-bottom: 20px;
}

/* Стили для скроллбара в модальном окне */
.modal-container::-webkit-scrollbar {
  width: 8px;
}

.modal-container::-webkit-scrollbar-track {
  background: #0a0a0a;
}

.modal-container::-webkit-scrollbar-thumb {
  background: #333;
  border-radius: 4px;
}

.modal-container::-webkit-scrollbar-thumb:hover {
  background: #666;
}

@media (max-width: 768px) {
  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
