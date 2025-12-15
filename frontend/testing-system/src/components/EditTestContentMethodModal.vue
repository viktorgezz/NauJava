<!--
  Модальное окно для выбора способа редактирования содержимого теста.
  Предоставляет три опции: визуальный конструктор, JSON редактор, нейросеть (недоступна).
-->
<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="handleClose">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">Выберите способ редактирования</h2>
        <button @click="handleClose" class="btn-close" aria-label="Закрыть">×</button>
      </div>

      <div class="modal-body">
        <div class="methods-list">
          <!-- Визуальный конструктор -->
          <div class="method-card" @click="handleVisualEditor">
            <div class="method-icon">🎨</div>
            <div class="method-content">
              <h3 class="method-title">Визуальный конструктор</h3>
              <p class="method-description">
                Редактируйте тест с помощью удобного интерфейса с пагинацией
              </p>
            </div>
            <div class="method-arrow">→</div>
          </div>

          <!-- JSON редактор -->
          <div class="method-card" @click="handleJsonEditor">
            <div class="method-icon">📝</div>
            <div class="method-content">
              <h3 class="method-title">Редактирование через JSON</h3>
              <p class="method-description">
                Вставьте JSON с вопросами и вариантами ответов
              </p>
            </div>
            <div class="method-arrow">→</div>
          </div>

          <!-- Нейросеть (недоступна) -->
          <div class="method-card method-card-disabled" @click="handleAiEditor">
            <div class="method-icon">🤖</div>
            <div class="method-content">
              <h3 class="method-title">Создание через нейросеть</h3>
              <p class="method-description">
                Автоматическое создание теста с помощью ИИ
              </p>
            </div>
            <div class="method-arrow">→</div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="handleClose" class="btn btn-secondary">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

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

const emit = defineEmits(['close'])

/**
 * Обработка закрытия модального окна
 */
const handleClose = () => {
  emit('close')
}

/**
 * Обработка выбора визуального редактора
 */
const handleVisualEditor = () => {
  handleClose()
  router.push(`/tests/${props.testId}/edit`)
}

/**
 * Обработка выбора JSON редактора
 */
const handleJsonEditor = () => {
  handleClose()
  router.push(`/tests/${props.testId}/edit/json`)
}

/**
 * Обработка выбора нейросети (пока недоступна)
 */
const handleAiEditor = () => {
  alert('Нейросеть в данный момент не доступна')
}
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
  z-index: 1001;
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
}

.methods-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.method-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid rgba(0, 255, 136, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.method-card:hover {
  background: rgba(0, 255, 136, 0.1);
  border-color: rgba(0, 255, 136, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.2);
}

.method-card-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.method-card-disabled:hover {
  transform: none;
  background: rgba(0, 0, 0, 0.3);
  border-color: rgba(0, 255, 136, 0.2);
  box-shadow: none;
}

.method-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.method-content {
  flex: 1;
  min-width: 0;
}

.method-title {
  color: #00ff88;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.method-description {
  color: #999;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

.method-arrow {
  color: #00ff88;
  font-size: 24px;
  flex-shrink: 0;
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

.btn-secondary {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
}

.btn-secondary:hover {
  background: #00ff88;
  color: #0a0a0a;
}

/* Стили для скроллбара */
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
  .modal-container {
    max-width: 100%;
  }

  .method-card {
    flex-direction: column;
    text-align: center;
  }

  .method-arrow {
    transform: rotate(90deg);
  }
}
</style>

