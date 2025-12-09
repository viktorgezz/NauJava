<!--
  Страница прохождения теста.
  Отображает вопросы теста, таймер и собирает ответы пользователя.
-->
<template>
  <div class="test-passing-container">
    <div class="test-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="test-title">Прохождение теста</h1>
          <div class="timer" :class="{ 'timer-warning': timeSpentSeconds > 3600 }">
            <span class="timer-icon">⏱</span>
            <span class="timer-value">{{ formattedTime }}</span>
          </div>
        </div>
        <div class="header-actions">
          <button @click="handleSubmitClick" class="btn btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? 'Отправка...' : 'Завершить тест' }}
          </button>
          <button @click="handleCancel" class="btn btn-secondary">Отменить</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Загрузка теста...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p class="error-text">{{ error }}</p>
      <button @click="loadTest" class="btn btn-secondary">Попробовать снова</button>
    </div>

    <div v-else class="test-content">
      <div class="progress-bar-container">
        <div class="progress-info">
          <span>Прогресс: {{ answeredCount }} / {{ totalQuestions }}</span>
          <span class="progress-percent">{{ progressPercent }}%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-bar-fill" :style="{ width: `${progressPercent}%` }"></div>
        </div>
      </div>

      <div class="questions-list">
        <div
          v-for="(question, index) in questions"
          :key="question.idQuestion"
          class="question-card"
          :class="{ 'question-answered': isQuestionAnswered(question.idQuestion) }"
          :id="`question-${question.idQuestion}`"
        >
          <div class="question-header">
            <span class="question-number">Вопрос {{ index + 1 }}</span>
            <span
              class="question-status"
              :class="{
                'status-answered': isQuestionAnswered(question.idQuestion),
                'status-unanswered': !isQuestionAnswered(question.idQuestion),
              }"
            >
              {{ isQuestionAnswered(question.idQuestion) ? '✓ Отвечен' : 'Не отвечен' }}
            </span>
          </div>

          <div class="question-text">{{ question.text }}</div>

          <!-- SINGLE_CHOICE -->
          <div v-if="question.type === 'SINGLE_CHOICE'" class="answer-options">
            <label
              v-for="option in question.answerOptions"
              :key="option.idAnswerOption"
              class="answer-option"
              :class="{ selected: isOptionSelected(question.idQuestion, option.idAnswerOption) }"
            >
              <input
                type="radio"
                :name="`question-${question.idQuestion}`"
                :value="option.idAnswerOption"
                :checked="isOptionSelected(question.idQuestion, option.idAnswerOption)"
                @change="selectSingleOption(question.idQuestion, option.idAnswerOption)"
              />
              <span class="option-text">{{ option.text }}</span>
            </label>
          </div>

          <!-- MULTIPLE_CHOICE -->
          <div v-else-if="question.type === 'MULTIPLE_CHOICE'" class="answer-options">
            <label
              v-for="option in question.answerOptions"
              :key="option.idAnswerOption"
              class="answer-option"
              :class="{ selected: isOptionSelected(question.idQuestion, option.idAnswerOption) }"
            >
              <input
                type="checkbox"
                :checked="isOptionSelected(question.idQuestion, option.idAnswerOption)"
                @change="toggleMultipleOption(question.idQuestion, option.idAnswerOption)"
              />
              <span class="option-text">{{ option.text }}</span>
            </label>
          </div>

          <!-- OPEN_TEXT -->
          <div v-else-if="question.type === 'OPEN_TEXT'" class="open-text-answer">
            <textarea
              class="answer-textarea"
              :value="getTextAnswer(question.idQuestion)"
              @input="setTextAnswer(question.idQuestion, $event.target.value)"
              placeholder="Введите ваш ответ..."
              rows="4"
            ></textarea>
          </div>
        </div>
      </div>

      <div class="bottom-actions">
        <button
          @click="handleSubmitClick"
          class="btn btn-primary btn-large"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? 'Отправка...' : 'Завершить тест' }}
        </button>
      </div>
    </div>

    <!-- Модальное окно предупреждения о неотвеченных вопросах -->
    <div v-if="showWarningModal" class="modal-overlay" @click.self="closeWarningModal">
      <div class="modal-container warning-modal">
        <div class="modal-header">
          <h2 class="modal-title warning-title">⚠ Предупреждение</h2>
          <button @click="closeWarningModal" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <p class="warning-text">Вы не ответили на следующие вопросы:</p>
          <div class="unanswered-list">
            <span
              v-for="num in unansweredQuestionNumbers"
              :key="num"
              class="unanswered-number"
              @click="scrollToQuestion(num)"
            >
              {{ num }}
            </span>
          </div>
          <p class="warning-subtitle">
            Вы уверены, что хотите завершить тест? Неотвеченные вопросы будут засчитаны как
            неправильные.
          </p>
        </div>
        <div class="modal-footer">
          <button @click="confirmSubmit" class="btn btn-danger" :disabled="isSubmitting">
            {{ isSubmitting ? 'Отправка...' : 'Да, завершить тест' }}
          </button>
          <button @click="closeWarningModal" class="btn btn-secondary">
            Нет, продолжить отвечать
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTestForPassing } from '@/api/testService'
import { submitTestResult } from '@/api/resultService'

const route = useRoute()
const router = useRouter()

const testId = computed(() => Number(route.params.id))

const questions = ref([])
const loading = ref(false)
const error = ref(null)
const isSubmitting = ref(false)
const showWarningModal = ref(false)
const isTestFinished = ref(false) // Флаг завершения/отмены теста

// Ответы пользователя: Map<idQuestion, { textAnswerWritten: string, idsSelectedAnswerOption: number[] }>
const userAnswers = ref({})

// Таймер
const timeSpentSeconds = ref(0)
let timerInterval = null

// Ключ для localStorage
const getStorageKey = () => `test-passing-${testId.value}`

/**
 * Форматирует время в формат HH:MM:SS
 */
const formattedTime = computed(() => {
  const hours = Math.floor(timeSpentSeconds.value / 3600)
  const minutes = Math.floor((timeSpentSeconds.value % 3600) / 60)
  const seconds = timeSpentSeconds.value % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

/**
 * Количество отвеченных вопросов
 */
const answeredCount = computed(() => {
  return questions.value.filter((q) => isQuestionAnswered(q.idQuestion)).length
})

/**
 * Общее количество вопросов
 */
const totalQuestions = computed(() => questions.value.length)

/**
 * Процент прогресса
 */
const progressPercent = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((answeredCount.value / totalQuestions.value) * 100)
})

/**
 * Номера неотвеченных вопросов
 */
const unansweredQuestionNumbers = computed(() => {
  return questions.value
    .map((q, index) => ({ question: q, number: index + 1 }))
    .filter(({ question }) => !isQuestionAnswered(question.idQuestion))
    .map(({ number }) => number)
})

/**
 * Проверяет, отвечен ли вопрос
 */
const isQuestionAnswered = (questionId) => {
  const answer = userAnswers.value[questionId]
  if (!answer) return false

  const question = questions.value.find((q) => q.idQuestion === questionId)
  if (!question) return false

  if (question.type === 'OPEN_TEXT') {
    return answer.textAnswerWritten && answer.textAnswerWritten.trim() !== ''
  } else {
    return answer.idsSelectedAnswerOption && answer.idsSelectedAnswerOption.length > 0
  }
}

/**
 * Проверяет, выбран ли вариант ответа
 */
const isOptionSelected = (questionId, optionId) => {
  const answer = userAnswers.value[questionId]
  if (!answer) return false
  return answer.idsSelectedAnswerOption && answer.idsSelectedAnswerOption.includes(optionId)
}

/**
 * Выбирает один вариант ответа (SINGLE_CHOICE)
 */
const selectSingleOption = (questionId, optionId) => {
  if (!userAnswers.value[questionId]) {
    userAnswers.value[questionId] = { textAnswerWritten: '', idsSelectedAnswerOption: [] }
  }
  userAnswers.value[questionId].idsSelectedAnswerOption = [optionId]
  saveToStorage()
}

/**
 * Переключает выбор варианта ответа (MULTIPLE_CHOICE)
 */
const toggleMultipleOption = (questionId, optionId) => {
  if (!userAnswers.value[questionId]) {
    userAnswers.value[questionId] = { textAnswerWritten: '', idsSelectedAnswerOption: [] }
  }
  const options = userAnswers.value[questionId].idsSelectedAnswerOption
  const index = options.indexOf(optionId)
  if (index === -1) {
    options.push(optionId)
  } else {
    options.splice(index, 1)
  }
  saveToStorage()
}

/**
 * Получает текстовый ответ
 */
const getTextAnswer = (questionId) => {
  return userAnswers.value[questionId]?.textAnswerWritten || ''
}

/**
 * Устанавливает текстовый ответ (OPEN_TEXT)
 */
const setTextAnswer = (questionId, text) => {
  if (!userAnswers.value[questionId]) {
    userAnswers.value[questionId] = { textAnswerWritten: '', idsSelectedAnswerOption: [] }
  }
  userAnswers.value[questionId].textAnswerWritten = text
  saveToStorage()
}

/**
 * Сохраняет состояние в localStorage
 */
const saveToStorage = () => {
  if (!testId.value) return
  try {
    const state = {
      userAnswers: userAnswers.value,
      timeSpentSeconds: timeSpentSeconds.value,
      timestamp: Date.now(),
    }
    localStorage.setItem(getStorageKey(), JSON.stringify(state))
  } catch (err) {
    console.warn('Не удалось сохранить состояние теста:', err)
  }
}

/**
 * Загружает состояние из localStorage
 */
const loadFromStorage = () => {
  if (!testId.value) return null
  try {
    const saved = localStorage.getItem(getStorageKey())
    if (!saved) return null

    const state = JSON.parse(saved)
    // Проверяем, что сохраненное состояние не старше 24 часов
    const maxAge = 24 * 60 * 60 * 1000
    if (Date.now() - state.timestamp > maxAge) {
      localStorage.removeItem(getStorageKey())
      return null
    }

    return state
  } catch (err) {
    console.warn('Не удалось загрузить состояние теста:', err)
    return null
  }
}

/**
 * Очищает сохраненное состояние
 */
const clearStorage = () => {
  if (!testId.value) return
  localStorage.removeItem(getStorageKey())
}

/**
 * Загружает тест
 */
const loadTest = async () => {
  if (!testId.value) {
    error.value = 'ID теста не указан'
    return
  }

  loading.value = true
  error.value = null

  try {
    const testData = await getTestForPassing(testId.value)
    questions.value = testData.questionsDto || []

    // Пытаемся восстановить сохраненное состояние
    const savedState = loadFromStorage()
    if (savedState) {
      userAnswers.value = savedState.userAnswers || {}
      timeSpentSeconds.value = savedState.timeSpentSeconds || 0
    } else {
      // Инициализируем пустые ответы для всех вопросов
      questions.value.forEach((q) => {
        userAnswers.value[q.idQuestion] = {
          textAnswerWritten: '',
          idsSelectedAnswerOption: [],
        }
      })
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Ошибка при загрузке теста'
  } finally {
    loading.value = false
  }
}

/**
 * Запускает таймер
 */
const startTimer = () => {
  if (timerInterval) return
  timerInterval = setInterval(() => {
    timeSpentSeconds.value++
    // Сохраняем каждые 10 секунд
    if (timeSpentSeconds.value % 10 === 0) {
      saveToStorage()
    }
  }, 1000)
}

/**
 * Останавливает таймер
 */
const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

/**
 * Обработка нажатия на кнопку завершения
 */
const handleSubmitClick = () => {
  if (unansweredQuestionNumbers.value.length > 0) {
    showWarningModal.value = true
  } else {
    submitTest()
  }
}

/**
 * Закрывает модальное окно предупреждения
 */
const closeWarningModal = () => {
  showWarningModal.value = false
}

/**
 * Подтверждение отправки теста
 */
const confirmSubmit = () => {
  showWarningModal.value = false
  submitTest()
}

/**
 * Прокрутка к вопросу
 */
const scrollToQuestion = (questionNumber) => {
  showWarningModal.value = false
  const question = questions.value[questionNumber - 1]
  if (question) {
    const element = document.getElementById(`question-${question.idQuestion}`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }
}

/**
 * Отправляет результат теста
 */
const submitTest = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    // Формируем данные для отправки
    const idQuestionToUserAnswers = {}
    questions.value.forEach((q) => {
      const answer = userAnswers.value[q.idQuestion] || {
        textAnswerWritten: '',
        idsSelectedAnswerOption: [],
      }
      idQuestionToUserAnswers[q.idQuestion] = {
        textAnswerWritten: answer.textAnswerWritten || '',
        idsSelectedAnswerOption: answer.idsSelectedAnswerOption || [],
      }
    })

    const resultData = {
      timeSpentSeconds: timeSpentSeconds.value,
      idTest: testId.value,
      idQuestionToUserAnswers,
    }

    const resultId = await submitTestResult(resultData)

    // Отмечаем тест как завершённый
    isTestFinished.value = true

    // Очищаем сохраненное состояние и сбрасываем
    clearStorage()
    resetTestState()
    stopTimer()

    // Переходим на страницу результата
    router.push(`/results/${resultId}`)
  } catch (err) {
    error.value = err.response?.data?.message || 'Ошибка при отправке результата'
    isSubmitting.value = false
  }
}

/**
 * Отмена прохождения теста
 */
const handleCancel = () => {
  if (confirm('Вы уверены, что хотите отменить прохождение теста? Ваш прогресс будет удалён.')) {
    isTestFinished.value = true
    clearStorage()
    resetTestState()
    stopTimer()
    router.push('/')
  }
}

/**
 * Сбрасывает состояние теста
 */
const resetTestState = () => {
  questions.value = []
  userAnswers.value = {}
  timeSpentSeconds.value = 0
  error.value = null
  isSubmitting.value = false
  showWarningModal.value = false
}

/**
 * Сохраняет состояние только если тест не завершён
 */
const saveIfNotFinished = () => {
  if (!isTestFinished.value) {
    saveToStorage()
  }
}

onMounted(() => {
  loadTest()
  startTimer()

  // Сохранение перед закрытием страницы (только если тест не завершён)
  window.addEventListener('beforeunload', saveIfNotFinished)
})

onBeforeUnmount(() => {
  stopTimer()
  // Сохраняем только если тест не был завершён/отменён
  if (!isTestFinished.value) {
    saveToStorage()
  }
  window.removeEventListener('beforeunload', saveIfNotFinished)
})

// Следим за изменением testId
watch(testId, () => {
  if (testId.value) {
    loadTest()
  }
})
</script>

<style scoped>
.test-passing-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  padding-bottom: 60px;
}

.test-header {
  background: #1a1a1a;
  border-bottom: 2px solid #333;
  padding: 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.test-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(0, 255, 136, 0.1);
  border: 2px solid #00ff88;
  border-radius: 12px;
  color: #00ff88;
  font-size: 18px;
  font-weight: 600;
  font-family: 'Monaco', 'Consolas', monospace;
}

.timer-warning {
  background: rgba(255, 170, 0, 0.1);
  border-color: #ffaa00;
  color: #ffaa00;
}

.timer-icon {
  font-size: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.loading-container,
.error-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #333;
  border-top-color: #00ff88;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text,
.error-text {
  color: #999;
  font-size: 18px;
}

.error-text {
  color: #ff4444;
  margin-bottom: 20px;
}

.test-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.progress-bar-container {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 30px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #e0e0e0;
  font-size: 14px;
}

.progress-percent {
  color: #00ff88;
  font-weight: 600;
}

.progress-bar {
  height: 8px;
  background: #333;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ff88, #00cc6a);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.question-card:hover {
  border-color: #444;
}

.question-answered {
  border-color: rgba(0, 255, 136, 0.3);
  background: rgba(0, 255, 136, 0.02);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #333;
}

.question-number {
  color: #00ff88;
  font-size: 16px;
  font-weight: 600;
}

.question-status {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 12px;
}

.status-answered {
  background: rgba(0, 255, 136, 0.15);
  color: #00ff88;
}

.status-unanswered {
  background: rgba(153, 153, 153, 0.15);
  color: #999;
}

.question-text {
  color: #e0e0e0;
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.answer-option {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  background: #0a0a0a;
  border: 2px solid #333;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.answer-option:hover {
  border-color: #00ff88;
  background: rgba(0, 255, 136, 0.03);
}

.answer-option.selected {
  border-color: #00ff88;
  background: rgba(0, 255, 136, 0.1);
}

.answer-option input[type='radio'],
.answer-option input[type='checkbox'] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #00ff88;
  flex-shrink: 0;
  margin-top: 2px;
}

.option-text {
  color: #e0e0e0;
  font-size: 16px;
  line-height: 1.5;
}

.open-text-answer {
  margin-top: 12px;
}

.answer-textarea {
  width: 100%;
  padding: 16px;
  background: #0a0a0a;
  border: 2px solid #333;
  border-radius: 12px;
  color: #e0e0e0;
  font-size: 16px;
  font-family: inherit;
  resize: vertical;
  min-height: 120px;
  transition: all 0.3s ease;
}

.answer-textarea:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.answer-textarea::placeholder {
  color: #666;
}

.bottom-actions {
  margin-top: 40px;
  text-align: center;
}

.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-large {
  padding: 18px 48px;
  font-size: 18px;
}

.btn-primary {
  background: #00ff88;
  color: #0a0a0a;
}

.btn-primary:hover:not(:disabled) {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 255, 136, 0.3);
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

.btn-danger {
  background: #ff4444;
  color: #fff;
}

.btn-danger:hover:not(:disabled) {
  background: #cc3333;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 68, 68, 0.3);
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Модальное окно предупреждения */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: rgba(26, 26, 26, 0.95);
  backdrop-filter: blur(20px);
  border: 2px solid rgba(0, 255, 136, 0.3);
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.warning-modal {
  border-color: rgba(255, 170, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #333;
}

.modal-title {
  color: #00ff88;
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.warning-title {
  color: #ffaa00;
}

.btn-close {
  background: transparent;
  border: none;
  color: #999;
  font-size: 28px;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: #333;
  color: #e0e0e0;
}

.modal-body {
  padding: 24px;
}

.warning-text {
  color: #e0e0e0;
  font-size: 16px;
  margin: 0 0 16px;
}

.warning-subtitle {
  color: #999;
  font-size: 14px;
  margin: 16px 0 0;
}

.unanswered-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.unanswered-number {
  padding: 8px 14px;
  background: rgba(255, 170, 0, 0.15);
  border: 1px solid #ffaa00;
  border-radius: 8px;
  color: #ffaa00;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.unanswered-number:hover {
  background: rgba(255, 170, 0, 0.25);
  transform: translateY(-2px);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #333;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }

  .header-left {
    justify-content: space-between;
  }

  .header-actions {
    justify-content: stretch;
  }

  .header-actions .btn {
    flex: 1;
  }

  .question-text {
    font-size: 16px;
  }

  .modal-footer {
    flex-direction: column;
  }

  .modal-footer .btn {
    width: 100%;
  }
}
</style>
