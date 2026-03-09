<!--
  Страница визуального конструктора для редактирования содержимого теста.
  Реализует пагинацию (10 вопросов на странице, максимум 30 вопросов).
-->
<template>
  <div class="editor-container">
    <div class="editor-header">
      <div class="header-content">
        <h1 class="editor-title">Редактирование содержимого теста</h1>
        <div class="header-actions">
          <button @click="handleSave" class="btn btn-primary" :disabled="isSaving">
            {{ isSaving ? 'Сохранение...' : 'Сохранить' }}
          </button>
          <button @click="handleCancel" class="btn btn-secondary">Отмена</button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Загрузка содержимого теста...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <div class="error-content">
        <p class="error-text" v-html="formatError(error)"></p>
      </div>
      <div class="error-actions">
        <button @click="loadTestContent(false)" class="btn btn-secondary">Загрузить с сервера</button>
        <button v-if="loadStateFromStorage()" @click="loadTestContent(true)" class="btn btn-primary">
          Восстановить из автосохранения
        </button>
      </div>
    </div>
    
    <div v-if="restoredFromStorage" class="restore-notification">
      <p>⚠️ Данные восстановлены из автосохранения. Не забудьте сохранить изменения!</p>
      <button @click="restoredFromStorage = false" class="btn-close-notification">×</button>
    </div>

    <div v-else class="editor-content">
      <div class="pagination-controls">
        <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="btn btn-pagination"
        >
          ← Предыдущая страница
        </button>
        <div class="page-info">
          <span>Страница {{ currentPage + 1 }} из {{ totalPages }}</span>
          <span class="questions-count">Вопросов: {{ totalQuestions }} / 30</span>
        </div>
        <button
          @click="goToNextPage"
          :disabled="!canGoToNextPage"
          class="btn btn-pagination"
        >
          Следующая страница →
        </button>
      </div>

      <div class="questions-container">
        <div
          v-for="(question, index) in currentPageQuestions"
          :key="question.tempId || question.idQuestion"
          class="question-card"
        >
          <div class="question-header">
            <h3 class="question-number">Вопрос {{ getQuestionNumber(index) }}</h3>
            <button
              @click="removeQuestion(index)"
              class="btn-remove-question"
              :disabled="currentPageQuestions.length === 1 && totalQuestions === 1"
            >
              ×
            </button>
          </div>

          <QuestionEditor
            :question="question"
            :question-index="getQuestionIndex(index)"
            @update="updateQuestion(getQuestionIndex(index), $event)"
          />
        </div>

        <button
          v-if="canAddQuestionOnCurrentPage"
          @click="addQuestion"
          class="btn-add-question"
        >
          + Добавить вопрос
        </button>
        <div v-else-if="totalQuestions >= 30" class="max-questions-notice">
          Достигнут максимум вопросов (30)
        </div>
        <div v-else-if="currentPageQuestions.length >= 10" class="page-full-notice">
          На этой странице уже 10 вопросов. Перейдите на следующую страницу, чтобы добавить еще вопросы.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTestContent, updateTestContent } from '@/api/testService'
import QuestionEditor from '@/components/QuestionEditor.vue'

const route = useRoute()
const router = useRouter()

const testId = computed(() => Number(route.params.id))
const questionsPerPage = 10
const maxQuestions = 30

const questions = ref([])
const loading = ref(false)
const error = ref(null)
const isSaving = ref(false)
const currentPage = ref(0)
const restoredFromStorage = ref(false)

// Ключ для сохранения состояния в localStorage
const getStorageKey = () => `test-editor-${testId.value}`

/**
 * Сохраняет состояние формы в localStorage
 */
const saveStateToStorage = () => {
  if (!testId.value || questions.value.length === 0) return
  
  try {
    const state = {
      questions: questions.value,
      currentPage: currentPage.value,
      timestamp: Date.now(),
    }
    localStorage.setItem(getStorageKey(), JSON.stringify(state))
  } catch (err) {
    console.warn('Не удалось сохранить состояние формы:', err)
  }
}

/**
 * Загружает состояние формы из localStorage
 */
const loadStateFromStorage = () => {
  if (!testId.value) return null
  
  try {
    const saved = localStorage.getItem(getStorageKey())
    if (!saved) return null
    
    const state = JSON.parse(saved)
    // Проверяем, что сохраненное состояние не старше 2 часов
    const twoHours = 2 * 60 * 60 * 1000
    if (Date.now() - state.timestamp > twoHours) {
      localStorage.removeItem(getStorageKey())
      return null
    }
    
    return state
  } catch (err) {
    console.warn('Не удалось загрузить состояние формы:', err)
    return null
  }
}

/**
 * Очищает сохраненное состояние
 */
const clearSavedState = () => {
  if (!testId.value) return
  localStorage.removeItem(getStorageKey())
}

/**
 * Вычисляет общее количество страниц (включая пустые страницы для создания новых вопросов)
 */
const totalPages = computed(() => {
  const pagesWithQuestions = Math.ceil(questions.value.length / questionsPerPage)
  // Если есть вопросы и текущая страница заполнена, разрешаем создать еще одну страницу
  const maxPossiblePages = Math.ceil(maxQuestions / questionsPerPage)
  return Math.max(1, Math.min(maxPossiblePages, Math.max(pagesWithQuestions, currentPage.value + 1)))
})

/**
 * Проверяет, можно ли перейти на следующую страницу
 */
const canGoToNextPage = computed(() => {
  // Нельзя перейти, если уже достигнут максимум вопросов
  if (totalQuestions.value >= maxQuestions) return false
  
  // Можно перейти, если следующая страница существует или можно создать новую
  const nextPage = currentPage.value + 1
  const maxPossiblePages = Math.ceil(maxQuestions / questionsPerPage)
  
  return nextPage < maxPossiblePages
})

/**
 * Вычисляет общее количество вопросов
 */
const totalQuestions = computed(() => {
  return questions.value.length
})

/**
 * Вычисляет вопросы для текущей страницы
 */
const currentPageQuestions = computed(() => {
  const start = currentPage.value * questionsPerPage
  const end = start + questionsPerPage
  return questions.value.slice(start, end)
})

/**
 * Проверяет, можно ли добавить вопрос на текущей странице
 */
const canAddQuestionOnCurrentPage = computed(() => {
  return currentPageQuestions.value.length < questionsPerPage && totalQuestions.value < maxQuestions
})

/**
 * Получает глобальный индекс вопроса
 */
const getQuestionIndex = (localIndex) => {
  return currentPage.value * questionsPerPage + localIndex
}

/**
 * Получает номер вопроса для отображения
 */
const getQuestionNumber = (localIndex) => {
  return getQuestionIndex(localIndex) + 1
}

/**
 * Загружает содержимое теста
 */
const loadTestContent = async (restoreFromStorage = false) => {
  if (!testId.value) {
    error.value = 'ID теста не указан'
    return
  }

  loading.value = true
  error.value = null

  try {
    // Пытаемся восстановить из localStorage, если указано
    if (restoreFromStorage) {
      const savedState = loadStateFromStorage()
      if (savedState) {
        questions.value = savedState.questions
        currentPage.value = savedState.currentPage || 0
        loading.value = false
        return
      }
    }

    const testContent = await getTestContent(testId.value)
    questions.value = (testContent.questions || []).map((q) => ({
      ...q,
      point: q.point || '1.00',
      correctTextAnswer: q.correctTextAnswer || [],
      answerOptions: (q.answerOptions || []).map((ao) => ({
        ...ao,
        tempId: ao.idAnswerOption ? null : `temp-${Date.now()}-${Math.random()}`,
      })),
      allowMistakes: q.allowMistakes || false,
      tempId: q.idQuestion ? null : `temp-${Date.now()}-${Math.random()}`,
    }))
    
    // Если есть сохраненное состояние, пытаемся его восстановить
    const savedState = loadStateFromStorage()
    if (savedState && savedState.questions.length > 0 && restoreFromStorage) {
      // Объединяем сохраненные изменения с загруженными данными
      questions.value = savedState.questions
      currentPage.value = savedState.currentPage || 0
      restoredFromStorage.value = true
    }
  } catch (err) {
    // Если ошибка 401, пытаемся восстановить из localStorage
    if (err.response?.status === 401) {
      const savedState = loadStateFromStorage()
      if (savedState) {
        questions.value = savedState.questions
        currentPage.value = savedState.currentPage || 0
        loading.value = false
        restoredFromStorage.value = true
        // Показываем предупреждение, что данные восстановлены из автосохранения
        error.value = 'Токен истек. Данные восстановлены из автосохранения. Пожалуйста, сохраните изменения.'
        return
      }
    }
    error.value = err.response?.data?.message || 'Ошибка при загрузке содержимого теста'
  } finally {
    loading.value = false
  }
}

/**
 * Добавляет новый вопрос
 */
const addQuestion = () => {
  if (questions.value.length >= maxQuestions) {
    return
  }

  const newQuestion = {
    idQuestion: null,
    text: '',
    type: 'SINGLE_CHOICE',
    point: '1.00',
    correctTextAnswer: [],
    answerOptions: [
      {
        idAnswerOption: null,
        text: '',
        isCorrect: false,
        explanation: null,
        tempId: `temp-${Date.now()}-1`,
      },
      {
        idAnswerOption: null,
        text: '',
        isCorrect: false,
        explanation: null,
        tempId: `temp-${Date.now()}-2`,
      },
    ],
    allowMistakes: false,
    tempId: `temp-${Date.now()}`,
  }

  questions.value.push(newQuestion)
  saveStateToStorage()
}

/**
 * Удаляет вопрос
 */
const removeQuestion = (localIndex) => {
  const globalIndex = getQuestionIndex(localIndex)
  questions.value.splice(globalIndex, 1)

  // Если на текущей странице не осталось вопросов и это не первая страница
  if (currentPageQuestions.value.length === 0 && currentPage.value > 0) {
    currentPage.value = currentPage.value - 1
  }
  
  saveStateToStorage()
}

/**
 * Обновляет вопрос
 */
const updateQuestion = (globalIndex, updatedQuestion) => {
  questions.value[globalIndex] = { ...questions.value[globalIndex], ...updatedQuestion }
  // Автосохранение при изменении
  saveStateToStorage()
}

/**
 * Переход на страницу
 */
const goToPage = (page) => {
  if (page >= 0) {
    currentPage.value = page
    saveStateToStorage()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

/**
 * Переход на следующую страницу
 */
const goToNextPage = () => {
  if (!canGoToNextPage.value) return
  
  const nextPage = currentPage.value + 1
  goToPage(nextPage)
}

/**
 * Валидация всех вопросов перед сохранением
 */
const validateAllQuestions = () => {
  const validationErrors = []

  questions.value.forEach((question, index) => {
    const questionNum = index + 1
    const errors = []

    // Проверка текста вопроса
    if (!question.text || question.text.trim() === '') {
      errors.push(`Вопрос ${questionNum}: текст вопроса не может быть пустым`)
    }

    // Проверка баллов
    const point = parseFloat(question.point)
    if (isNaN(point) || point < 0) {
      errors.push(`Вопрос ${questionNum}: баллы должны быть не менее 0`)
    }

    // Проверка для SINGLE_CHOICE и MULTIPLE_CHOICE
    if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
      // Проверка наличия вариантов ответов
      if (!question.answerOptions || question.answerOptions.length === 0) {
        errors.push(`Вопрос ${questionNum}: должен содержать хотя бы один вариант ответа`)
      } else {
        // Проверка текста вариантов ответов
        question.answerOptions.forEach((option, optIndex) => {
          if (!option.text || option.text.trim() === '') {
            errors.push(
              `Вопрос ${questionNum}, вариант ответа ${optIndex + 1}: текст не может быть пустым`,
            )
          }
        })

        // Проверка правильных ответов
        const correctCount = question.answerOptions.filter((ao) => ao.isCorrect).length
        if (question.type === 'SINGLE_CHOICE' && correctCount !== 1) {
          errors.push(
            `Вопрос ${questionNum}: для вопроса с одиночным выбором должен быть выбран ровно один правильный ответ`,
          )
        } else if (question.type === 'MULTIPLE_CHOICE' && correctCount === 0) {
          errors.push(
            `Вопрос ${questionNum}: для вопроса с множественным выбором должен быть выбран хотя бы один правильный ответ`,
          )
        }
      }
    }

    // Проверка для OPEN_TEXT
    if (question.type === 'OPEN_TEXT') {
      if (!question.correctTextAnswer || question.correctTextAnswer.length === 0) {
        errors.push(`Вопрос ${questionNum}: должен содержать хотя бы один правильный ответ`)
      } else {
        question.correctTextAnswer.forEach((answer, ansIndex) => {
          if (answer && answer.length > 150) {
            errors.push(
              `Вопрос ${questionNum}, правильный ответ ${ansIndex + 1}: длина не должна превышать 150 символов`,
            )
          }
        })
      }
    }

    if (errors.length > 0) {
      validationErrors.push(...errors)
    }
  })

  return validationErrors
}

/**
 * Сохранение содержимого теста
 */
const handleSave = async () => {
  if (!testId.value) return

  // Валидация всех вопросов перед сохранением
  const validationErrors = validateAllQuestions()
  if (validationErrors.length > 0) {
    error.value = 'Ошибки валидации:\n' + validationErrors.join('\n')
    // Прокручиваем к началу, чтобы пользователь увидел ошибки
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }

  isSaving.value = true
  error.value = null

  try {
    // Подготавливаем данные для отправки
    const testContent = {
      idTest: testId.value,
      questions: questions.value.map((q) => ({
        idQuestion: q.idQuestion || null,
        text: q.text,
        type: q.type,
        point: String(q.point || '1.00'),
        correctTextAnswer: q.correctTextAnswer || [],
        answerOptions: (q.answerOptions || []).map((ao) => ({
          idAnswerOption: ao.idAnswerOption || null,
          text: ao.text,
          isCorrect: ao.isCorrect || false,
          explanation: ao.explanation || null,
        })),
        allowMistakes: q.type === 'MULTIPLE_CHOICE' ? (q.allowMistakes || false) : false,
      })),
    }

    await updateTestContent(testContent)
    // Очищаем сохраненное состояние после успешного сохранения
    clearSavedState()
    router.push('/')
  } catch (err) {
    // Если ошибка 401, пытаемся повторить запрос после обновления токена
    if (err.response?.status === 401) {
      // Интерцептор должен автоматически обновить токен и повторить запрос
      // Но на всякий случай сохраняем состояние
      saveStateToStorage()
      error.value = 'Токен истек. Попробуйте сохранить еще раз.'
    } else {
      error.value = err.response?.data?.message || 'Ошибка при сохранении содержимого теста'
    }
  } finally {
    isSaving.value = false
  }
}

/**
 * Форматирование ошибки для отображения
 */
const formatError = (errorText) => {
  if (!errorText) return ''
  // Если ошибка содержит переносы строк, заменяем их на <br>
  return errorText.replace(/\n/g, '<br>')
}

/**
 * Отмена редактирования
 */
const handleCancel = () => {
  router.push('/')
}

let saveInterval = null

onMounted(() => {
  // Пытаемся сначала восстановить из localStorage
  const savedState = loadStateFromStorage()
  if (savedState && savedState.questions.length > 0) {
    // Загружаем с восстановлением из хранилища
    loadTestContent(true)
  } else {
    // Загружаем с сервера
    loadTestContent(false)
  }
  
  // Автосохранение при изменении вопросов
  saveInterval = setInterval(() => {
    if (questions.value.length > 0) {
      saveStateToStorage()
    }
  }, 30000) // Сохраняем каждые 30 секунд
  
  // Сохранение перед закрытием страницы
  window.addEventListener('beforeunload', saveStateToStorage)
})

onBeforeUnmount(() => {
  // Очистка интервала
  if (saveInterval) {
    clearInterval(saveInterval)
  }
  // Удаляем обработчик события
  window.removeEventListener('beforeunload', saveStateToStorage)
  // Сохраняем состояние перед уходом со страницы
  saveStateToStorage()
})
</script>

<style scoped>
.editor-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  padding-bottom: 40px;
}

.editor-header {
  background: #1a1a1a;
  border-bottom: 2px solid #333;
  padding: 20px;
  margin-bottom: 30px;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.editor-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.loading-container,
.error-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 60px 20px;
  text-align: center;
}

.error-content {
  max-width: 800px;
  margin: 0 auto 20px;
  padding: 20px;
  background: rgba(255, 68, 68, 0.1);
  border: 2px solid #ff4444;
  border-radius: 12px;
  text-align: left;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
  flex-wrap: wrap;
}

.restore-notification {
  max-width: 1400px;
  margin: 0 auto 20px;
  padding: 16px 20px;
  background: rgba(255, 170, 0, 0.2);
  border: 2px solid #ffaa00;
  border-radius: 12px;
  color: #ffaa00;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.restore-notification p {
  margin: 0;
  flex: 1;
}

.btn-close-notification {
  background: transparent;
  border: none;
  color: #ffaa00;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.btn-close-notification:hover {
  background: rgba(255, 170, 0, 0.2);
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
.error-text {
  color: #999;
  font-size: 18px;
}

.error-text {
  color: #ff4444;
  margin-bottom: 20px;
}

.editor-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.page-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #e0e0e0;
  font-size: 14px;
}

.questions-count {
  color: #00ff88;
  font-weight: 600;
}

.questions-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.question-card:hover {
  border-color: #00ff88;
  box-shadow: 0 4px 16px rgba(0, 255, 136, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #333;
}

.question-number {
  color: #00ff88;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.btn-remove-question {
  background: transparent;
  border: 2px solid #ff4444;
  color: #ff4444;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-remove-question:hover:not(:disabled) {
  background: #ff4444;
  color: #0a0a0a;
}

.btn-remove-question:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-add-question {
  background: transparent;
  border: 2px dashed #00ff88;
  color: #00ff88;
  padding: 20px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-add-question:hover {
  background: rgba(0, 255, 136, 0.1);
  border-style: solid;
}

.max-questions-notice {
  padding: 16px;
  background: rgba(255, 170, 0, 0.1);
  border: 2px solid #ffaa00;
  border-radius: 12px;
  color: #ffaa00;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
}

.page-full-notice {
  padding: 16px;
  background: rgba(0, 255, 136, 0.1);
  border: 2px solid #00ff88;
  border-radius: 12px;
  color: #00ff88;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
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

.btn-pagination {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
  min-width: 150px;
}

.btn-pagination:hover:not(:disabled) {
  background: #00ff88;
  color: #0a0a0a;
}

.btn-pagination:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  border-color: #666;
  color: #666;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .pagination-controls {
    flex-direction: column;
  }

  .page-info {
    order: -1;
  }
}
</style>

