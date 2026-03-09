<!--
  Компонент для редактирования вопроса теста.
  Поддерживает три типа вопросов: SINGLE_CHOICE, MULTIPLE_CHOICE, OPEN_TEXT.
-->
<template>
  <div class="question-editor">
    <div class="form-group">
      <label class="form-label">Текст вопроса <span class="required">*</span></label>
      <textarea
        v-model="localQuestion.text"
        class="form-textarea"
        :class="{ 'form-input-error': errors.text }"
        placeholder="Введите текст вопроса"
        rows="3"
        @input="updateQuestion"
      ></textarea>
      <span v-if="errors.text" class="error-message">{{ errors.text }}</span>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label class="form-label">Тип вопроса <span class="required">*</span></label>
        <select v-model="localQuestion.type" class="form-select" @change="handleTypeChange">
          <option value="SINGLE_CHOICE">Одиночный выбор</option>
          <option value="MULTIPLE_CHOICE">Множественный выбор</option>
          <option value="OPEN_TEXT">Открытый ответ</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">Баллы <span class="required">*</span></label>
        <input
          v-model.number="localQuestion.point"
          type="number"
          step="0.01"
          min="0"
          class="form-input"
          :class="{ 'form-input-error': errors.point }"
          placeholder="1.00"
          @input="updateQuestion"
        />
        <span v-if="errors.point" class="error-message">{{ errors.point }}</span>
      </div>
    </div>

    <!-- Варианты ответов для SINGLE_CHOICE и MULTIPLE_CHOICE -->
    <div v-if="localQuestion.type === 'SINGLE_CHOICE' || localQuestion.type === 'MULTIPLE_CHOICE'">
      <div class="form-group">
        <label class="form-label">Варианты ответов</label>
        <div
          v-for="(option, index) in localQuestion.answerOptions"
          :key="option.tempId || option.idAnswerOption"
          class="answer-option-item"
        >
          <div class="answer-option-content">
            <input
              v-model="option.text"
              type="text"
              class="form-input answer-option-input"
              :class="{ 'form-input-error': errors.answerOptions && errors.answerOptions[index] }"
              :placeholder="`Вариант ответа ${index + 1}`"
              @input="updateQuestion"
            />
            <label class="checkbox-label">
              <input
                v-if="localQuestion.type === 'SINGLE_CHOICE'"
                v-model="correctAnswerIndex"
                :value="index"
                type="radio"
                :name="`question-${questionIndex}-correct`"
                @change="handleCorrectChange(index)"
              />
              <input
                v-else
                v-model="option.isCorrect"
                type="checkbox"
                @change="handleCorrectChange(index)"
              />
              <span>Правильный</span>
            </label>
            <input
              v-model="option.explanation"
              type="text"
              class="form-input explanation-input"
              placeholder="Объяснение (необязательно)"
              @input="updateQuestion"
            />
            <button
              @click="removeAnswerOption(index)"
              class="btn-remove-option"
              :disabled="localQuestion.answerOptions.length <= 2"
            >
              ×
            </button>
          </div>
        </div>
        <button
          v-if="localQuestion.answerOptions.length < 10"
          @click="addAnswerOption"
          class="btn-add-option"
        >
          + Добавить вариант ответа
        </button>
        <div v-if="errors.answerOptions && errors.answerOptions.general" class="error-message">
          {{ errors.answerOptions.general }}
        </div>
        <div v-else-if="localQuestion.type === 'SINGLE_CHOICE'" class="info-hint">
          Для вопроса с одиночным выбором должен быть выбран ровно один правильный ответ
        </div>
        <div v-else-if="localQuestion.type === 'MULTIPLE_CHOICE'" class="info-hint">
          Для вопроса с множественным выбором должен быть выбран хотя бы один правильный ответ
        </div>
        <div v-if="localQuestion.type === 'MULTIPLE_CHOICE'" class="form-group allow-mistakes-group">
          <label class="checkbox-label allow-mistakes-label">
            <input
              v-model="localQuestion.allowMistakes"
              type="checkbox"
              @change="updateQuestion"
            />
            <span>Разрешить ошибки (частичное начисление баллов)</span>
          </label>
          <div class="allow-mistakes-hint">
            Если включено, при неправильных ответах баллы начисляются частично
          </div>
        </div>
      </div>
    </div>

    <!-- Правильные ответы для OPEN_TEXT -->
    <div v-if="localQuestion.type === 'OPEN_TEXT'">
      <div class="form-group">
        <label class="form-label">Правильные ответы (до 5)</label>
        <div
          v-for="(answer, index) in localQuestion.correctTextAnswer"
          :key="index"
          class="correct-answer-item"
        >
          <input
            v-model="localQuestion.correctTextAnswer[index]"
            type="text"
            class="form-input"
            :class="{ 'form-input-error': errors.correctTextAnswer && errors.correctTextAnswer[index] }"
            :placeholder="`Правильный ответ ${index + 1}`"
            maxlength="150"
            @input="updateQuestion"
          />
          <button @click="removeCorrectAnswer(index)" class="btn-remove-option">
            ×
          </button>
        </div>
        <button
          v-if="localQuestion.correctTextAnswer.length < 5"
          @click="addCorrectAnswer"
          class="btn-add-option"
        >
          + Добавить правильный ответ
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

const props = defineProps({
  question: {
    type: Object,
    required: true,
  },
  questionIndex: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['update'])

const localQuestion = ref({
  idQuestion: props.question.idQuestion || null,
  text: props.question.text || '',
  type: props.question.type || 'SINGLE_CHOICE',
  point: props.question.point || '1.00',
  correctTextAnswer: props.question.correctTextAnswer || [],
  answerOptions: (props.question.answerOptions || []).map((ao) => ({
    idAnswerOption: ao.idAnswerOption || null,
    text: ao.text || '',
    isCorrect: ao.isCorrect || false,
    explanation: ao.explanation || null,
    tempId: ao.tempId || (ao.idAnswerOption ? null : `temp-${Date.now()}`),
  })),
  allowMistakes: props.question.allowMistakes || false,
})

const errors = reactive({
  text: '',
  point: '',
  answerOptions: {},
  correctTextAnswer: {},
})

/**
 * Computed для индекса правильного ответа (для SINGLE_CHOICE с radio)
 */
const correctAnswerIndex = computed({
  get() {
    if (localQuestion.value.type === 'SINGLE_CHOICE') {
      const index = localQuestion.value.answerOptions.findIndex((ao) => ao.isCorrect)
      return index >= 0 ? index : null
    }
    return null
  },
  set(value) {
    if (value !== null && localQuestion.value.type === 'SINGLE_CHOICE') {
      localQuestion.value.answerOptions.forEach((option, index) => {
        option.isCorrect = index === value
      })
      updateQuestion()
    }
  },
})

/**
 * Обновляет вопрос и отправляет изменения родителю
 */
const updateQuestion = () => {
  validateQuestion()
  emit('update', { ...localQuestion.value })
}

/**
 * Валидация вопроса
 */
const validateQuestion = () => {
  errors.text = ''
  errors.point = ''
  errors.answerOptions = {}
  errors.correctTextAnswer = {}

  if (!localQuestion.value.text || localQuestion.value.text.trim() === '') {
    errors.text = 'Текст вопроса не может быть пустым'
  }

  const point = parseFloat(localQuestion.value.point)
  if (isNaN(point) || point < 0) {
    errors.point = 'Баллы должны быть не менее 0'
  }

  if (
    localQuestion.value.type === 'SINGLE_CHOICE' ||
    localQuestion.value.type === 'MULTIPLE_CHOICE'
  ) {
    localQuestion.value.answerOptions.forEach((option, index) => {
      if (!option.text || option.text.trim() === '') {
        if (!errors.answerOptions[index]) {
          errors.answerOptions[index] = {}
        }
        errors.answerOptions[index].text = 'Текст варианта ответа не может быть пустым'
      }
    })

    // Проверка правильных ответов
    const correctCount = localQuestion.value.answerOptions.filter((ao) => ao.isCorrect).length
    
    if (localQuestion.value.type === 'SINGLE_CHOICE') {
      // Для SINGLE_CHOICE: должен быть ровно один правильный ответ
      if (correctCount !== 1) {
        errors.answerOptions = { general: 'Для вопроса с одиночным выбором должен быть выбран ровно один правильный ответ' }
      }
    } else if (localQuestion.value.type === 'MULTIPLE_CHOICE') {
      // Для MULTIPLE_CHOICE: должен быть хотя бы один правильный ответ
      if (correctCount === 0) {
        errors.answerOptions = { general: 'Для вопроса с множественным выбором должен быть выбран хотя бы один правильный ответ' }
      }
    }
  }

  if (localQuestion.value.type === 'OPEN_TEXT') {
    localQuestion.value.correctTextAnswer.forEach((answer, index) => {
      if (answer && answer.length > 150) {
        if (!errors.correctTextAnswer[index]) {
          errors.correctTextAnswer[index] = {}
        }
        errors.correctTextAnswer[index] = 'Длина ответа не должна превышать 150 символов'
      }
    })
  }
}

/**
 * Обработка изменения типа вопроса
 */
const handleTypeChange = () => {
  // При смене типа очищаем неактуальные данные
  if (localQuestion.value.type === 'OPEN_TEXT') {
    localQuestion.value.answerOptions = []
    localQuestion.value.allowMistakes = false
  } else {
    localQuestion.value.correctTextAnswer = []
    // Если нет вариантов ответов, добавляем минимум 2
    if (localQuestion.value.answerOptions.length === 0) {
      addAnswerOption()
      addAnswerOption()
    }
    // Сбрасываем allowMistakes для SINGLE_CHOICE
    if (localQuestion.value.type === 'SINGLE_CHOICE') {
      localQuestion.value.allowMistakes = false
    }
  }
  updateQuestion()
}

/**
 * Добавление варианта ответа
 */
const addAnswerOption = () => {
  if (localQuestion.value.answerOptions.length >= 10) return

  localQuestion.value.answerOptions.push({
    idAnswerOption: null,
    text: '',
    isCorrect: false,
    explanation: null,
    tempId: `temp-${Date.now()}`,
  })
  updateQuestion()
}

/**
 * Удаление варианта ответа
 */
const removeAnswerOption = (index) => {
  if (localQuestion.value.answerOptions.length <= 2) return
  localQuestion.value.answerOptions.splice(index, 1)
  updateQuestion()
}

/**
 * Обработка изменения правильности ответа
 */
const handleCorrectChange = (changedIndex) => {
  if (localQuestion.value.type === 'SINGLE_CHOICE') {
    // Для SINGLE_CHOICE с radio это обрабатывается через computed
    // Но на всякий случай убеждаемся, что только один выбран
    localQuestion.value.answerOptions.forEach((option, index) => {
      option.isCorrect = index === changedIndex
    })
  } else if (localQuestion.value.type === 'MULTIPLE_CHOICE') {
    // Для MULTIPLE_CHOICE просто обновляем состояние
    // isCorrect уже обновлен через v-model
  }
  updateQuestion()
}

/**
 * Добавление правильного ответа для OPEN_TEXT
 */
const addCorrectAnswer = () => {
  if (localQuestion.value.correctTextAnswer.length >= 5) return
  localQuestion.value.correctTextAnswer.push('')
  updateQuestion()
}

/**
 * Удаление правильного ответа для OPEN_TEXT
 */
const removeCorrectAnswer = (index) => {
  localQuestion.value.correctTextAnswer.splice(index, 1)
  updateQuestion()
}

// Инициализация при первом рендере
if (
  (localQuestion.value.type === 'SINGLE_CHOICE' ||
    localQuestion.value.type === 'MULTIPLE_CHOICE') &&
  localQuestion.value.answerOptions.length === 0
) {
  addAnswerOption()
  addAnswerOption()
}

// Следим за изменениями props
watch(
  () => props.question,
  (newQuestion) => {
    localQuestion.value = {
      idQuestion: newQuestion.idQuestion || null,
      text: newQuestion.text || '',
      type: newQuestion.type || 'SINGLE_CHOICE',
      point: newQuestion.point || '1.00',
      correctTextAnswer: newQuestion.correctTextAnswer || [],
      answerOptions: (newQuestion.answerOptions || []).map((ao) => ({
        idAnswerOption: ao.idAnswerOption || null,
        text: ao.text || '',
        isCorrect: ao.isCorrect || false,
        explanation: ao.explanation || null,
        tempId: ao.tempId || (ao.idAnswerOption ? null : `temp-${Date.now()}`),
      })),
      allowMistakes: newQuestion.allowMistakes || false,
    }
  },
  { deep: true },
)
</script>

<style scoped>
.question-editor {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-label {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: #ff4444;
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
  min-height: 80px;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.form-input-error {
  border-color: #ff4444;
}

.error-message {
  color: #ff4444;
  font-size: 13px;
  margin-top: 4px;
}

.answer-option-item,
.correct-answer-item {
  margin-bottom: 12px;
}

.answer-option-content,
.correct-answer-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.answer-option-input {
  flex: 1;
}

.explanation-input {
  flex: 1;
  max-width: 300px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e0e0e0;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.checkbox-label input[type='checkbox'] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #00ff88;
}

.btn-remove-option {
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
  flex-shrink: 0;
}

.btn-remove-option:hover:not(:disabled) {
  background: #ff4444;
  color: #0a0a0a;
}

.btn-remove-option:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-add-option {
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

.btn-add-option:hover {
  background: rgba(0, 255, 136, 0.1);
  border-style: solid;
}

.info-hint {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
  padding: 8px;
  background: rgba(0, 255, 136, 0.05);
  border: 1px solid rgba(0, 255, 136, 0.2);
  border-radius: 8px;
}

.allow-mistakes-group {
  margin-top: 16px;
  padding: 16px;
  background: rgba(0, 255, 136, 0.05);
  border: 1px solid rgba(0, 255, 136, 0.2);
  border-radius: 8px;
}

.allow-mistakes-label {
  font-size: 15px;
  font-weight: 500;
  color: #00ff88;
}

.allow-mistakes-hint {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
  font-style: italic;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .answer-option-content,
  .correct-answer-item {
    flex-direction: column;
  }

  .explanation-input {
    max-width: 100%;
  }
}
</style>

