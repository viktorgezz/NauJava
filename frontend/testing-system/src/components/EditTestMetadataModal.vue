<!--
  Модальное окно для редактирования метаданных теста.
  Реализует валидацию полей согласно требованиям бэкенда (TestMetadataDto)
  и интеграцию с API для обновления метаданных теста.
-->
<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="handleClose">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">Изменить описание теста</h2>
        <button @click="handleClose" class="btn-close" aria-label="Закрыть">×</button>
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">Загрузка данных...</p>
      </div>

      <form v-else @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-group">
          <label for="title" class="form-label">
            Название теста <span class="required">*</span>
          </label>
          <input
            id="title"
            v-model="title"
            type="text"
            class="form-input"
            :class="{ 'form-input-error': errors.title }"
            placeholder="Введите название теста"
            @blur="validateTitle"
            @input="validateTitle"
            maxlength="100"
          />
          <div class="input-hint">
            <span v-if="errors.title" class="error-message">{{ errors.title }}</span>
            <span
              v-else
              class="char-count"
              :class="{ 'char-count-warning': (title?.length || 0) > 90 }"
            >
              {{ title?.length || 0 }} / 100 символов
            </span>
          </div>
        </div>

        <div class="form-group">
          <label for="description" class="form-label">Описание теста</label>
          <textarea
            id="description"
            v-model="description"
            class="form-textarea"
            :class="{ 'form-input-error': errors.description }"
            placeholder="Введите описание теста (необязательно)"
            rows="4"
            @blur="validateDescription"
            @input="validateDescription"
          ></textarea>
          <div class="input-hint">
            <span v-if="errors.description" class="error-message">{{ errors.description }}</span>
            <span
              v-else
              class="char-count"
              :class="{ 'char-count-warning': (description?.length || 0) > 300 }"
            >
              {{ description?.length || 0 }} / 325 символов
            </span>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Статус</label>
          <select v-model="status" class="form-select">
            <option value="PRIVATE">Приватный</option>
            <option value="PUBLIC">Публичный</option>
            <option value="UNLISTED">По ссылке</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label"> Темы теста <span class="hint">(от 0 до 5)</span> </label>
          <div class="topics-container">
            <div v-for="(topic, index) in topics" :key="index" class="topic-input-wrapper">
              <input
                v-model="topics[index]"
                type="text"
                class="form-input topic-input"
                :class="{ 'form-input-error': errors.topics && errors.topics[index] }"
                :placeholder="`Тема ${index + 1}`"
                @blur="validateTopics"
                @input="validateTopics"
              />
              <button
                v-if="topics.length > 0"
                type="button"
                @click="removeTopic(index)"
                class="btn-remove-topic"
                aria-label="Удалить тему"
              >
                ×
              </button>
            </div>
            <button v-if="topics.length < 5" type="button" @click="addTopic" class="btn-add-topic">
              + Добавить тему
            </button>
          </div>
          <span v-if="errors.topics && typeof errors.topics === 'string'" class="error-message">
            {{ errors.topics }}
          </span>
        </div>

        <div v-if="submitError" class="error-message error-message-global">
          {{ submitError }}
        </div>

        <div class="modal-actions">
          <button type="button" @click="handleClose" class="btn btn-secondary">Отмена</button>
          <button type="submit" class="btn btn-primary" :disabled="isSubmitting || !isFormValid">
            {{ isSubmitting ? 'Сохранение...' : 'Сохранить' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { updateTestMetadata, getTestById } from '@/api/testService'

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

const emit = defineEmits(['close', 'success'])

const title = ref('')
const description = ref('')
const status = ref('PRIVATE')
const topics = ref([])
const isSubmitting = ref(false)
const loading = ref(false)
const submitError = ref('')

const errors = reactive({
  title: '',
  description: '',
  topics: null,
})

/**
 * Валидация названия теста согласно TestMetadataDto
 */
const validateTitle = () => {
  errors.title = ''

  if (!title.value || title.value.trim() === '') {
    errors.title = 'Название теста не может быть пустым'
    return false
  }

  const trimmedTitle = title.value.trim()
  if (trimmedTitle.length < 1 || trimmedTitle.length > 100) {
    errors.title = 'Название теста должно быть от 1 до 100 символов'
    return false
  }

  return true
}

/**
 * Валидация описания теста согласно TestMetadataDto
 */
const validateDescription = () => {
  errors.description = ''

  if (description.value && description.value.trim().length > 325) {
    errors.description = 'Описание не может превышать 325 символов'
    return false
  }

  return true
}

/**
 * Валидация списка тем согласно TestMetadataDto
 */
const validateTopics = () => {
  errors.topics = null

  // Проверка количества тем (максимум 5)
  if (topics.value.length > 5) {
    errors.topics = 'Количество тем не может быть более 5'
    return false
  }

  // Проверка пустых тем
  const emptyTopics = topics.value.some((topic) => !topic || topic.trim() === '')
  if (emptyTopics) {
    errors.topics = 'Все темы должны быть заполнены или удалены'
    return false
  }

  return true
}

/**
 * Проверка валидности всей формы
 */
const isFormValid = computed(() => {
  const trimmedTitle = title.value?.trim() || ''
  const isTitleValid = trimmedTitle.length >= 1 && trimmedTitle.length <= 100

  const trimmedDescription = description.value?.trim() || ''
  const isDescriptionValid = trimmedDescription.length <= 325

  const allowedStatuses = ['PRIVATE', 'PUBLIC', 'UNLISTED']
  const isStatusValid = allowedStatuses.includes(status.value)

  // Темы могут быть от 0 до 5, но если есть темы, они не должны быть пустыми
  const validTopics = topics.value.filter((t) => t && t.trim() !== '')
  const isTopicsValid = topics.value.length <= 5 && validTopics.length === topics.value.length

  return isTitleValid && isDescriptionValid && isStatusValid && isTopicsValid
})

/**
 * Добавление новой темы
 */
const addTopic = () => {
  if (topics.value.length < 5) {
    topics.value.push('')
  }
}

/**
 * Удаление темы
 */
const removeTopic = (index) => {
  topics.value.splice(index, 1)
  validateTopics()
}

/**
 * Валидация всех полей формы
 */
const validateForm = () => {
  const isTitleValid = validateTitle()
  const isDescriptionValid = validateDescription()
  const isTopicsValid = validateTopics()
  return isTitleValid && isDescriptionValid && isTopicsValid
}

/**
 * Загрузка данных теста
 */
const loadTestData = async () => {
  if (!props.testId) return

  loading.value = true
  submitError.value = ''

  try {
    const testData = await getTestById(props.testId)
    title.value = testData.title || ''
    description.value = testData.description || ''
    status.value = testData.status || 'PRIVATE'
    topics.value = testData.namesTopics ? [...testData.namesTopics] : []
  } catch (err) {
    submitError.value = err.response?.data?.message || 'Ошибка при загрузке данных теста'
  } finally {
    loading.value = false
  }
}

/**
 * Обработка закрытия модального окна
 */
const handleClose = () => {
  if (isSubmitting.value) return
  resetForm()
  emit('close')
}

/**
 * Сброс формы
 */
const resetForm = () => {
  title.value = ''
  description.value = ''
  status.value = 'PRIVATE'
  topics.value = []
  submitError.value = ''
  errors.title = ''
  errors.description = ''
  errors.topics = null
}

/**
 * Обработка отправки формы
 */
const handleSubmit = async () => {
  submitError.value = ''

  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    // Фильтруем пустые темы
    const validTopics = topics.value.filter((topic) => topic && topic.trim() !== '')

    const testId = await updateTestMetadata({
      idTest: props.testId,
      title: title.value.trim(),
      description: description.value.trim() || null,
      status: status.value,
      titlesTopic: validTopics,
    })

    if (testId) {
      resetForm()
      emit('success', testId)
    } else {
      submitError.value = 'Ошибка при обновлении теста'
    }
  } catch (error) {
    submitError.value =
      error.response?.data?.message || error.message || 'Произошла ошибка при обновлении теста'
  } finally {
    isSubmitting.value = false
  }
}

// Загружаем данные при открытии модального окна
watch(
  () => [props.isOpen, props.testId],
  ([newIsOpen, newTestId]) => {
    if (newIsOpen && newTestId) {
      loadTestData()
    } else if (!newIsOpen) {
      resetForm()
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
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
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
}

.btn-close:hover {
  background: #333;
  color: #e0e0e0;
}

.loading-container {
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

.loading-text {
  color: #999;
  font-size: 16px;
}

.modal-form {
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: #ff4444;
}

.hint {
  color: #999;
  font-weight: 400;
  font-size: 12px;
}

.form-input,
.form-textarea,
.form-select {
  padding: 12px 16px;
  background: #0a0a0a;
  border: 1px solid #333;
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 16px;
  font-family: inherit;
  transition: all 0.3s ease;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: #666;
}

.form-input-error {
  border-color: #ff4444;
}

.input-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
}

.char-count {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}

.char-count-warning {
  color: #ffaa00;
}

.status-hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.error-message {
  color: #ff4444;
  font-size: 13px;
  margin-top: 4px;
}

.error-message-global {
  text-align: center;
  padding: 12px;
  background: rgba(255, 68, 68, 0.1);
  border: 1px solid #ff4444;
  border-radius: 8px;
}

.topics-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.topic-input {
  flex: 1;
}

.btn-remove-topic {
  background: transparent;
  border: 2px solid #ff4444;
  color: #ff4444;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.btn-remove-topic:hover {
  background: #ff4444;
  color: #0a0a0a;
}

.btn-add-topic {
  background: transparent;
  border: 2px dashed #00ff88;
  color: #00ff88;
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  align-self: flex-start;
}

.btn-add-topic:hover {
  background: rgba(0, 255, 136, 0.1);
  border-style: solid;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  padding-top: 20px;
  border-top: 1px solid #333;
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

.btn-primary:hover:not(:disabled) {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
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
</style>
