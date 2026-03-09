<!--
  Страница отображения результата прохождения теста.
  Показывает набранные баллы, оценку, время и детализацию ответов.
-->
<template>
  <div class="result-container">
    <div class="result-header">
      <div class="header-content">
        <h1 class="result-title">Результаты теста</h1>
        <button @click="goToHome" class="btn btn-primary">На главную</button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Загрузка результатов...</p>
      <p class="loading-subtext">Подождите, результаты обрабатываются</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p class="error-text">{{ error }}</p>
      <div class="error-actions">
        <button @click="loadResult" class="btn btn-secondary">Попробовать снова</button>
        <button @click="goToHome" class="btn btn-primary">На главную</button>
      </div>
    </div>

    <div v-else-if="result" class="result-content">
      <!-- Сводка результатов -->
      <div class="result-summary">
        <div class="summary-header">
          <div class="grade-circle" :class="getGradeClass(result.grade)">
            <span class="grade-letter">{{ result.grade }}</span>
          </div>
          <div class="summary-info">
            <h2 class="summary-title">{{ getGradeTitle(result.grade) }}</h2>
            <p class="summary-subtitle">Тест завершён</p>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div class="stat-content">
              <span class="stat-label">Набрано баллов</span>
              <span class="stat-value"
                >{{ formatScore(result.score) }} / {{ formatScore(result.scoreMax) }}</span
              >
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">📈</div>
            <div class="stat-content">
              <span class="stat-label">Процент</span>
              <span class="stat-value" :class="getPercentClass(scorePercent)"
                >{{ scorePercent }}%</span
              >
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">⏱</div>
            <div class="stat-content">
              <span class="stat-label">Время прохождения</span>
              <span class="stat-value">{{ formattedTime }}</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">📅</div>
            <div class="stat-content">
              <span class="stat-label">Дата завершения</span>
              <span class="stat-value">{{ formattedDate }}</span>
            </div>
          </div>
        </div>

        <div class="answers-summary">
          <div class="answers-bar">
            <div class="answers-bar-correct" :style="{ width: `${correctPercent}%` }"></div>
          </div>
          <div class="answers-legend">
            <span class="legend-item correct">
              <span class="legend-dot"></span>
              Правильно: {{ correctCount }}
            </span>
            <span class="legend-item incorrect">
              <span class="legend-dot"></span>
              Неправильно: {{ incorrectCount }}
            </span>
          </div>
        </div>
      </div>

      <!-- Детализация ответов -->
      <div class="answers-details">
        <h3 class="details-title">Детализация ответов</h3>

        <div
          v-for="(question, index) in result.questionsDto"
          :key="question.idQuestion"
          class="question-card"
          :class="{
            'question-correct': isQuestionCorrect(question),
            'question-partially-correct': isQuestionPartiallyCorrect(question),
            'question-incorrect': !isQuestionCorrect(question) && !isQuestionPartiallyCorrect(question),
          }"
        >
          <div class="question-header">
            <span class="question-number">Вопрос {{ index + 1 }}</span>
            <span
              class="question-status"
              :class="{
                'status-correct': isQuestionCorrect(question),
                'status-partially-correct': isQuestionPartiallyCorrect(question),
                'status-incorrect': !isQuestionCorrect(question) && !isQuestionPartiallyCorrect(question),
              }"
            >
              <template v-if="isQuestionCorrect(question)">✓ Правильно</template>
              <template v-else-if="isQuestionPartiallyCorrect(question)">⚠ Частично правильно</template>
              <template v-else>✗ Неправильно</template>
            </span>
          </div>

          <div class="question-text">{{ question.text }}</div>

          <!-- Варианты ответов для SINGLE_CHOICE и MULTIPLE_CHOICE -->
          <div
            v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'"
            class="answer-options"
          >
            <div
              v-for="option in question.userAnswersDto"
              :key="option.idAnswerOption"
              class="answer-option"
              :class="getOptionClass(option)"
            >
              <div class="option-content">
                <span class="option-text">{{ option.textOption }}</span>
                <div class="option-badges">
                  <span v-if="option.isSelected" class="badge badge-selected">Ваш выбор</span>
                  <span v-if="option.isCorrect" class="badge badge-correct">Правильный</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Ответ для OPEN_TEXT -->
          <div v-else-if="question.type === 'OPEN_TEXT'" class="open-text-answer">
            <div class="user-answer-section">
              <span class="section-label">Ваш ответ:</span>
              <div
                class="user-text-answer"
                :class="{
                  'answer-correct': isOpenTextCorrect(question),
                  'answer-incorrect': !isOpenTextCorrect(question),
                  'answer-empty': !getUserTextAnswer(question),
                }"
              >
                {{ getUserTextAnswer(question) || 'Ответ не дан' }}
              </div>
            </div>
            <div v-if="getCorrectTextAnswers(question).length > 0" class="correct-answer-section">
              <span class="section-label">Правильные варианты:</span>
              <div class="correct-answers-list">
                <span
                  v-for="(answer, i) in getCorrectTextAnswers(question)"
                  :key="i"
                  class="correct-answer-item"
                >
                  {{ answer }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Кнопка возврата на главную -->
      <div class="bottom-actions">
        <button @click="goToHome" class="btn btn-primary btn-large">Вернуться на главную</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTestResult } from '@/api/resultService'

const route = useRoute()
const router = useRouter()

const resultId = computed(() => Number(route.params.id))

const result = ref(null)
const loading = ref(false)
const error = ref(null)

// Интервал для повторных запросов при ожидании результата
let pollInterval = null
const maxPollAttempts = 30 // максимум 30 попыток (30 секунд)
let pollAttempts = 0

/**
 * Форматирует балл
 */
const formatScore = (score) => {
  if (score === null || score === undefined) return '—'
  return Number(score).toFixed(2)
}

/**
 * Вычисляет процент набранных баллов
 */
const scorePercent = computed(() => {
  if (!result.value || !result.value.scoreMax || result.value.scoreMax === 0) return 0
  return Math.round((result.value.score / result.value.scoreMax) * 100)
})

/**
 * Форматирует время
 */
const formattedTime = computed(() => {
  if (!result.value) return '—'
  const seconds = result.value.timeSpentSeconds || 0
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
})

/**
 * Форматирует дату
 */
const formattedDate = computed(() => {
  if (!result.value || !result.value.completedAt) return '—'
  try {
    const date = new Date(result.value.completedAt)
    return date.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return result.value.completedAt
  }
})

/**
 * Подсчитывает количество правильных вопросов
 */
const correctCount = computed(() => {
  if (!result.value || !result.value.questionsDto) return 0
  return result.value.questionsDto.filter(isQuestionCorrect).length
})

/**
 * Подсчитывает количество неправильных вопросов
 */
const incorrectCount = computed(() => {
  if (!result.value || !result.value.questionsDto) return 0
  return result.value.questionsDto.length - correctCount.value
})

/**
 * Процент правильных ответов
 */
const correctPercent = computed(() => {
  const total = correctCount.value + incorrectCount.value
  if (total === 0) return 0
  return Math.round((correctCount.value / total) * 100)
})

/**
 * Проверяет, правильно ли отвечен вопрос
 */
const isQuestionCorrect = (question) => {
  if (!question || !question.userAnswersDto) return false

  if (question.type === 'OPEN_TEXT') {
    return isOpenTextCorrect(question)
  }

  // Для вопросов с выбором: все выбранные должны быть правильными И все правильные должны быть выбраны
  const selected = question.userAnswersDto.filter((opt) => opt.isSelected)
  const correct = question.userAnswersDto.filter((opt) => opt.isCorrect)

  if (selected.length === 0) return false
  if (selected.length !== correct.length) return false

  return selected.every((opt) => opt.isCorrect)
}

/**
 * Проверяет, является ли вопрос частично правильным (для MULTIPLE_CHOICE с allowMistakes)
 */
const isQuestionPartiallyCorrect = (question) => {
  if (!question || !question.userAnswersDto) return false
  if (question.type !== 'MULTIPLE_CHOICE') return false
  if (!question.allowMistakes) return false

  const selected = question.userAnswersDto.filter((opt) => opt.isSelected)
  const correct = question.userAnswersDto.filter((opt) => opt.isCorrect)
  const selectedCorrect = selected.filter((opt) => opt.isCorrect)

  if (selected.length === 0) return false
  if (selected.length === correct.length && selected.every((opt) => opt.isCorrect)) return false

  return selectedCorrect.length > 0
}

/**
 * Проверяет правильность открытого текстового ответа
 */
const isOpenTextCorrect = (question) => {
  if (!question.userAnswersDto || question.userAnswersDto.length === 0) return false

  // Для OPEN_TEXT проверяем поле isCorrect первого элемента
  const firstAnswer = question.userAnswersDto[0]
  return firstAnswer?.isCorrect === true
}

/**
 * Получает текст ответа пользователя для OPEN_TEXT
 */
const getUserTextAnswer = (question) => {
  if (!question.userAnswersDto || question.userAnswersDto.length === 0) return null

  // Берём userTextAnswer из первого элемента
  return question.userAnswersDto[0]?.userTextAnswer || null
}

/**
 * Получает правильные текстовые ответы для OPEN_TEXT
 */
const getCorrectTextAnswers = (question) => {
  if (!question.userAnswersDto || question.userAnswersDto.length === 0) return []

  // Берём textAnswersTrue из первого элемента
  const firstAnswer = question.userAnswersDto[0]
  return firstAnswer?.textAnswersTrue || []
}

/**
 * Возвращает класс для варианта ответа
 */
const getOptionClass = (option) => {
  if (option.isSelected && option.isCorrect) {
    return 'option-correct-selected'
  }
  if (option.isSelected && !option.isCorrect) {
    return 'option-wrong-selected'
  }
  if (!option.isSelected && option.isCorrect) {
    return 'option-correct-missed'
  }
  return 'option-neutral'
}

/**
 * Возвращает класс для оценки
 */
const getGradeClass = (grade) => {
  const classes = {
    A: 'grade-a',
    B: 'grade-b',
    C: 'grade-c',
    F: 'grade-f',
  }
  return classes[grade] || 'grade-f'
}

/**
 * Возвращает заголовок для оценки
 */
const getGradeTitle = (grade) => {
  const titles = {
    A: 'Отлично!',
    B: 'Хорошо!',
    C: 'Удовлетворительно',
    F: 'Неудовлетворительно',
  }
  return titles[grade] || 'Результат'
}

/**
 * Возвращает класс для процента
 */
const getPercentClass = (percent) => {
  if (percent >= 95) return 'percent-excellent'
  if (percent >= 85) return 'percent-good'
  if (percent >= 70) return 'percent-satisfactory'
  return 'percent-fail'
}

/**
 * Загружает результат
 */
const loadResult = async () => {
  if (!resultId.value) {
    error.value = 'ID результата не указан'
    return
  }

  loading.value = true
  error.value = null
  pollAttempts = 0

  try {
    const data = await getTestResult(resultId.value)

    // Если результат еще не готов (score = null), ждем
    if (data.score === null || data.grade === null) {
      startPolling()
    } else {
      stopPolling()
      result.value = data
      loading.value = false
    }
  } catch (err) {
    stopPolling()
    error.value = err.response?.data?.message || 'Ошибка при загрузке результата'
    loading.value = false
  }
}

/**
 * Запускает опрос результата
 */
const startPolling = () => {
  if (pollInterval) return

  pollInterval = setInterval(async () => {
    pollAttempts++

    if (pollAttempts >= maxPollAttempts) {
      stopPolling()
      loading.value = false
      error.value = 'Превышено время ожидания результата. Попробуйте обновить страницу.'
      return
    }

    try {
      const data = await getTestResult(resultId.value)

      if (data.score !== null && data.grade !== null) {
        stopPolling()
        result.value = data
        loading.value = false
      }
    } catch (err) {
      stopPolling()
      error.value = err.response?.data?.message || 'Ошибка при загрузке результата'
      loading.value = false
    }
  }, 1000)
}

/**
 * Останавливает опрос
 */
const stopPolling = () => {
  if (pollInterval) {
    clearInterval(pollInterval)
    pollInterval = null
  }
}

/**
 * Переход на главную
 */
const goToHome = () => {
  router.push('/')
}

onMounted(() => {
  loadResult()
})

watch(resultId, () => {
  if (resultId.value) {
    loadResult()
  }
})
</script>

<style scoped>
.result-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  padding-bottom: 60px;
}

.result-header {
  background: #1a1a1a;
  border-bottom: 2px solid #333;
  padding: 20px;
}

.header-content {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-title {
  color: #00ff88;
  font-size: 26px;
  font-weight: 600;
  margin: 0;
}

.loading-container,
.error-container {
  max-width: 900px;
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
  margin: 0 0 8px;
}

.loading-subtext {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.error-text {
  color: #ff4444;
  font-size: 18px;
  margin-bottom: 24px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.result-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.result-summary {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #333;
}

.grade-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  flex-shrink: 0;
}

.grade-a {
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.2), rgba(0, 204, 106, 0.3));
  border: 3px solid #00ff88;
  color: #00ff88;
}

.grade-b {
  background: linear-gradient(135deg, rgba(0, 170, 255, 0.2), rgba(0, 136, 204, 0.3));
  border: 3px solid #00aaff;
  color: #00aaff;
}

.grade-c {
  background: linear-gradient(135deg, rgba(255, 170, 0, 0.2), rgba(204, 136, 0, 0.3));
  border: 3px solid #ffaa00;
  color: #ffaa00;
}

.grade-f {
  background: linear-gradient(135deg, rgba(255, 68, 68, 0.2), rgba(204, 51, 51, 0.3));
  border: 3px solid #ff4444;
  color: #ff4444;
}

.grade-letter {
  text-shadow: 0 2px 8px currentColor;
}

.summary-info {
  flex: 1;
}

.summary-title {
  color: #e0e0e0;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 4px;
}

.summary-subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: rgba(10, 10, 10, 0.6);
  border: 1px solid #333;
  border-radius: 12px;
}

.stat-icon {
  font-size: 28px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  color: #666;
  font-size: 13px;
}

.stat-value {
  color: #e0e0e0;
  font-size: 18px;
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

.answers-summary {
  padding-top: 20px;
  border-top: 1px solid #333;
}

.answers-bar {
  height: 12px;
  background: #ff4444;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 12px;
}

.answers-bar-correct {
  height: 100%;
  background: linear-gradient(90deg, #00ff88, #00cc6a);
  border-radius: 6px;
  transition: width 0.5s ease;
}

.answers-legend {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-item.correct {
  color: #00ff88;
}

.legend-item.incorrect {
  color: #ff4444;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: currentColor;
}

.answers-details {
  margin-bottom: 40px;
}

.details-title {
  color: #00ff88;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px;
}

.question-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.question-correct {
  border-color: rgba(0, 255, 136, 0.4);
}

.question-partially-correct {
  border-color: rgba(255, 170, 0, 0.4);
}

.question-incorrect {
  border-color: rgba(255, 68, 68, 0.4);
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
  font-size: 14px;
  padding: 6px 14px;
  border-radius: 10px;
  font-weight: 500;
}

.status-correct {
  background: rgba(0, 255, 136, 0.15);
  color: #00ff88;
}

.status-partially-correct {
  background: rgba(255, 170, 0, 0.15);
  color: #ffaa00;
}

.status-incorrect {
  background: rgba(255, 68, 68, 0.15);
  color: #ff4444;
}

.question-text {
  color: #e0e0e0;
  font-size: 17px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.answer-option {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.option-correct-selected {
  background: rgba(0, 255, 136, 0.1);
  border-color: rgba(0, 255, 136, 0.4);
}

.option-wrong-selected {
  background: rgba(255, 68, 68, 0.1);
  border-color: rgba(255, 68, 68, 0.4);
}

.option-correct-missed {
  background: rgba(255, 170, 0, 0.08);
  border-color: rgba(255, 170, 0, 0.3);
}

.option-neutral {
  background: rgba(10, 10, 10, 0.4);
  border-color: #333;
}

.option-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-text {
  color: #e0e0e0;
  font-size: 15px;
  line-height: 1.5;
}

.option-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-selected {
  background: rgba(0, 170, 255, 0.2);
  color: #00aaff;
}

.badge-correct {
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
}

.open-text-answer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-answer-section,
.correct-answer-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  color: #888;
  font-size: 13px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.user-text-answer {
  padding: 16px;
  border-radius: 10px;
  font-size: 15px;
  line-height: 1.5;
}

.user-text-answer.answer-correct {
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.3);
  color: #00ff88;
}

.user-text-answer.answer-incorrect {
  background: rgba(255, 68, 68, 0.1);
  border: 1px solid rgba(255, 68, 68, 0.3);
  color: #ff4444;
}

.user-text-answer.answer-empty {
  background: rgba(10, 10, 10, 0.6);
  border: 1px solid #333;
  color: #666;
  font-style: italic;
}

.correct-answers-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.correct-answer-item {
  padding: 8px 14px;
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 8px;
  color: #00ff88;
  font-size: 14px;
}

.bottom-actions {
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

.btn-primary:hover {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 255, 136, 0.3);
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
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .summary-header {
    flex-direction: column;
    text-align: center;
  }

  .grade-circle {
    width: 80px;
    height: 80px;
    font-size: 36px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .answers-legend {
    flex-direction: column;
    gap: 8px;
    align-items: center;
  }

  .question-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .answer-option {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
