<!--
  Страница для редактирования содержимого теста через JSON.
  Позволяет вставить JSON с вопросами, валидирует его и отправляет на сервер.
-->
<template>
  <div class="editor-container">
    <div class="editor-header">
      <div class="header-content">
        <h1 class="editor-title">Редактирование теста через JSON</h1>
        <div class="header-actions">
          <button @click="handleCopyPrompt" class="btn btn-secondary">
            📋 Скопировать промпт для нейросети
          </button>
          <button @click="handleSave" class="btn btn-primary" :disabled="isSaving || !isValidJson">
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

    <div v-else class="editor-content">
      <div class="json-editor-section">
        <div class="section-header">
          <h2 class="section-title">JSON содержимое теста</h2>
          <div class="validation-status" :class="validationStatusClass">
            <span v-if="validationErrors.length === 0 && jsonText.trim() !== ''">
              ✓ JSON валиден
            </span>
            <span v-else-if="jsonText.trim() === ''">
              Введите JSON
            </span>
            <span v-else>
              ✗ Ошибки валидации
            </span>
          </div>
        </div>

        <textarea
          v-model="jsonText"
          class="json-textarea"
          placeholder="Вставьте JSON с вопросами теста..."
          @input="validateJson"
        ></textarea>

        <div v-if="validationErrors.length > 0" class="validation-errors">
          <h3 class="errors-title">Ошибки валидации:</h3>
          <ul class="errors-list">
            <li v-for="(error, index) in validationErrors" :key="index" class="error-item">
              {{ error }}
            </li>
          </ul>
        </div>

        <div class="json-info">
          <p class="info-text">
            <strong>Формат JSON:</strong> Вставьте JSON в формате TestUpdateContentDto.
            Максимальное количество вопросов: 30.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTestContent, updateTestContentJson, getTestById } from '@/api/testService'

const route = useRoute()
const router = useRouter()

const testId = computed(() => Number(route.params.id))
const jsonText = ref('')
const loading = ref(false)
const isSaving = ref(false)
const validationErrors = ref([])
const testTitle = ref('Название теста')

/**
 * Класс для статуса валидации
 */
const validationStatusClass = computed(() => {
  if (jsonText.value.trim() === '') return 'status-empty'
  if (validationErrors.value.length === 0) return 'status-valid'
  return 'status-invalid'
})

/**
 * Проверка валидности JSON
 */
const isValidJson = computed(() => {
  return jsonText.value.trim() !== '' && validationErrors.value.length === 0
})

/**
 * Валидация JSON
 */
const validateJson = () => {
  validationErrors.value = []

  if (jsonText.value.trim() === '') {
    return
  }

  try {
    // Проверка синтаксиса JSON
    const parsed = JSON.parse(jsonText.value)

    // Проверка структуры
    if (!parsed.questions || !Array.isArray(parsed.questions)) {
      validationErrors.value.push('JSON должен содержать массив "questions"')
      return
    }

    // Проверка количества вопросов
    if (parsed.questions.length > 30) {
      validationErrors.value.push(
        `Количество вопросов (${parsed.questions.length}) превышает максимум (30)`,
      )
    }

    if (parsed.questions.length === 0) {
      validationErrors.value.push('Тест должен содержать хотя бы один вопрос')
    }

    // Валидация каждого вопроса
    parsed.questions.forEach((question, index) => {
      const questionNum = index + 1

      // Проверка обязательных полей
      if (!question.text || question.text.trim() === '') {
        validationErrors.value.push(`Вопрос ${questionNum}: текст вопроса не может быть пустым`)
      }

      // Проверка типа вопроса
      const validTypes = ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'OPEN_TEXT']
      if (!question.type || !validTypes.includes(question.type)) {
        validationErrors.value.push(
          `Вопрос ${questionNum}: тип вопроса должен быть одним из: ${validTypes.join(', ')}`,
        )
      }

      // Проверка баллов
      if (question.point === undefined || question.point === null) {
        validationErrors.value.push(`Вопрос ${questionNum}: поле "point" обязательно`)
      } else {
        const point = parseFloat(question.point)
        if (isNaN(point) || point < 0) {
          validationErrors.value.push(`Вопрос ${questionNum}: баллы должны быть не менее 0`)
        }
      }

      // Проверка для SINGLE_CHOICE и MULTIPLE_CHOICE
      if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
        if (!question.answerOptions || !Array.isArray(question.answerOptions)) {
          validationErrors.value.push(
            `Вопрос ${questionNum}: должен содержать массив "answerOptions"`,
          )
        } else if (question.answerOptions.length === 0) {
          validationErrors.value.push(
            `Вопрос ${questionNum}: должен содержать хотя бы один вариант ответа`,
          )
        } else {
          // Проверка вариантов ответов
          question.answerOptions.forEach((option, optIndex) => {
            if (!option.text || option.text.trim() === '') {
              validationErrors.value.push(
                `Вопрос ${questionNum}, вариант ответа ${optIndex + 1}: текст не может быть пустым`,
              )
            }
          })

          // Проверка правильных ответов
          const correctCount = question.answerOptions.filter((ao) => ao.isCorrect).length
          if (question.type === 'SINGLE_CHOICE' && correctCount !== 1) {
            validationErrors.value.push(
              `Вопрос ${questionNum}: для вопроса с одиночным выбором должен быть выбран ровно один правильный ответ`,
            )
          } else if (question.type === 'MULTIPLE_CHOICE' && correctCount === 0) {
            validationErrors.value.push(
              `Вопрос ${questionNum}: для вопроса с множественным выбором должен быть выбран хотя бы один правильный ответ`,
            )
          }
        }
      }

      // Проверка для OPEN_TEXT
      if (question.type === 'OPEN_TEXT') {
        if (!question.correctTextAnswer || !Array.isArray(question.correctTextAnswer)) {
          validationErrors.value.push(
            `Вопрос ${questionNum}: должен содержать массив "correctTextAnswer"`,
          )
        } else if (question.correctTextAnswer.length === 0) {
          validationErrors.value.push(
            `Вопрос ${questionNum}: должен содержать хотя бы один правильный ответ`,
          )
        } else {
          // Проверка длины правильных ответов
          question.correctTextAnswer.forEach((answer, ansIndex) => {
            if (answer && answer.length > 150) {
              validationErrors.value.push(
                `Вопрос ${questionNum}, правильный ответ ${ansIndex + 1}: длина не должна превышать 150 символов`,
              )
            }
          })

          // Проверка количества правильных ответов
          if (question.correctTextAnswer.length > 5) {
            validationErrors.value.push(
              `Вопрос ${questionNum}: количество правильных ответов не должно превышать 5`,
            )
          }
        }
      }
    })
  } catch (err) {
    validationErrors.value.push(`Ошибка парсинга JSON: ${err.message}`)
  }
}

/**
 * Генерация промпта для нейросети
 */
const generatePrompt = () => {
  // Определяем количество вопросов из текущего JSON или используем максимум
  let questionsCount = 30
  try {
    if (jsonText.value.trim() !== '') {
      const parsed = JSON.parse(jsonText.value)
      if (parsed.questions && Array.isArray(parsed.questions)) {
        const currentCount = parsed.questions.length
        questionsCount = currentCount > 0 ? currentCount : 30
      }
    }
  } catch (err) {
    // Если JSON невалиден, используем максимум
    questionsCount = 30
  }

  const prompt = `${testTitle.value}
Количество вопросов: до ${questionsCount}

Правила составления JSON:

1. Типы вопросов:
   - SINGLE_CHOICE: вопрос с одиночным выбором (должен быть ровно один правильный ответ)
   - MULTIPLE_CHOICE: вопрос с множественным выбором (может быть несколько правильных ответов)
   - OPEN_TEXT: открытый вопрос (правильный ответ вводится текстом)

2. Структура JSON:
   - idTest: null (для нового теста) или ID существующего теста
   - questions: массив вопросов (максимум 30)

3. Структура вопроса:
   - idQuestion: null (для нового вопроса) или ID существующего вопроса
   - text: текст вопроса (обязательно, не может быть пустым)
   - type: тип вопроса - SINGLE_CHOICE, MULTIPLE_CHOICE или OPEN_TEXT (обязательно)
   - point: количество баллов (обязательно, число >= 0)
   - correctTextAnswer: массив правильных текстовых ответов (для OPEN_TEXT, максимум 5, длина каждого <= 150 символов)
   - answerOptions: массив вариантов ответов (для SINGLE_CHOICE и MULTIPLE_CHOICE)
   - allowMistakes: разрешены ли ошибки (только для MULTIPLE_CHOICE)

4. Структура варианта ответа (answerOptions):
   - idAnswerOption: null (для нового варианта) или ID существующего варианта
   - text: текст варианта ответа (обязательно, не может быть пустым)
   - isCorrect: является ли вариант правильным (true/false)
   - explanation: пояснение к ответу (может быть null)

5. Правила валидации:
   - Для SINGLE_CHOICE: должен быть ровно один вариант с isCorrect: true
   - Для MULTIPLE_CHOICE: должен быть хотя бы один вариант с isCorrect: true
   - Для OPEN_TEXT: массив correctTextAnswer должен содержать хотя бы один ответ
   - Количество вопросов: максимум 30
   - Количество правильных ответов для OPEN_TEXT: максимум 5
   - Длина правильного ответа для OPEN_TEXT: максимум 150 символов

Шаблон JSON:

{
  "idTest": null,
  "questions": [
    {
      "idQuestion": null,
      "text": "Текст нового вопроса",
      "type": "SINGLE_CHOICE",
      "point": 5.0,
      "correctTextAnswer": [],
      "answerOptions": [
        {
          "idAnswerOption": null,
          "text": "Первый вариант ответа",
          "isCorrect": true,
          "explanation": "Пояснение к ответу"
        },
        {
          "idAnswerOption": null,
          "text": "Второй вариант ответа",
          "isCorrect": false,
          "explanation": null
        }
      ],
      "allowMistakes": false
    }
  ]
}`

  return prompt
}

/**
 * Копирование промпта в буфер обмена
 */
const handleCopyPrompt = async () => {
  const prompt = generatePrompt()
  try {
    await navigator.clipboard.writeText(prompt)
    alert('Промпт скопирован в буфер обмена!')
  } catch (err) {
    console.error('Ошибка при копировании:', err)
    // Fallback для старых браузеров
    const textArea = document.createElement('textarea')
    textArea.value = prompt
    textArea.style.position = 'fixed'
    textArea.style.opacity = '0'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      alert('Промпт скопирован в буфер обмена!')
    } catch (e) {
      alert('Не удалось скопировать промпт. Пожалуйста, скопируйте вручную.')
    }
    document.body.removeChild(textArea)
  }
}

/**
 * Загрузка текущего содержимого теста
 */
const loadTestContent = async () => {
  if (!testId.value) {
    return
  }

  loading.value = true
  try {
    // Загружаем метаданные теста для получения названия
    try {
      const testMetadata = await getTestById(testId.value)
      testTitle.value = testMetadata.title || 'Название теста'
    } catch (err) {
      console.warn('Не удалось загрузить метаданные теста:', err)
    }

    // Загружаем содержимое теста
    const testContent = await getTestContent(testId.value)
    // Форматируем JSON с отступами
    jsonText.value = JSON.stringify(
      {
        idTest: testContent.idTest,
        questions: testContent.questions || [],
      },
      null,
      2,
    )
    validateJson()
  } catch (err) {
    console.error('Ошибка при загрузке содержимого теста:', err)
    // Устанавливаем шаблон по умолчанию
    jsonText.value = JSON.stringify(
      {
        idTest: testId.value,
        questions: [],
      },
      null,
      2,
    )
  } finally {
    loading.value = false
  }
}

/**
 * Сохранение JSON
 */
const handleSave = async () => {
  if (!testId.value || !isValidJson.value) {
    return
  }

  isSaving.value = true
  try {
    await updateTestContentJson(testId.value, jsonText.value)
    router.push('/')
  } catch (err) {
    alert(err.response?.data?.message || 'Ошибка при сохранении теста')
  } finally {
    isSaving.value = false
  }
}

/**
 * Отмена редактирования
 */
const handleCancel = () => {
  router.push('/')
}

onMounted(() => {
  loadTestContent()
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
  flex-wrap: wrap;
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
  flex-wrap: wrap;
}

.loading-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 60px 20px;
  text-align: center;
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

.loading-text {
  color: #999;
  font-size: 18px;
}

.editor-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.json-editor-section {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 12px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  color: #00ff88;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.validation-status {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
}

.validation-status.status-empty {
  background: rgba(153, 153, 153, 0.2);
  color: #999;
  border: 1px solid #666;
}

.validation-status.status-valid {
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
  border: 1px solid #00ff88;
}

.validation-status.status-invalid {
  background: rgba(255, 68, 68, 0.2);
  color: #ff4444;
  border: 1px solid #ff4444;
}

.json-textarea {
  width: 100%;
  min-height: 500px;
  padding: 16px;
  background: #0a0a0a;
  border: 2px solid #333;
  border-radius: 8px;
  color: #e0e0e0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.json-textarea:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.validation-errors {
  margin-top: 20px;
  padding: 16px;
  background: rgba(255, 68, 68, 0.1);
  border: 2px solid #ff4444;
  border-radius: 8px;
}

.errors-title {
  color: #ff4444;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
}

.errors-list {
  margin: 0;
  padding-left: 20px;
  list-style-type: disc;
}

.error-item {
  color: #ff8888;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.error-item:last-child {
  margin-bottom: 0;
}

.json-info {
  margin-top: 20px;
  padding: 16px;
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.3);
  border-radius: 8px;
}

.info-text {
  color: #e0e0e0;
  font-size: 14px;
  margin: 0;
  line-height: 1.6;
}

.info-text strong {
  color: #00ff88;
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

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .btn {
    flex: 1;
    min-width: 120px;
  }

  .json-textarea {
    min-height: 400px;
  }
}
</style>

