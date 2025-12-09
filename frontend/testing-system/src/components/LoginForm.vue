<!--
  Компонент формы входа в систему.
  Реализует валидацию полей согласно требованиям бэкенда (AuthenticationRequest DTO)
  и интеграцию с Pinia store для управления аутентификацией.
-->
<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">Вход в систему</h2>

      <form @submit.prevent="handleSubmit" class="auth-form">
        <div class="form-group">
          <label for="name" class="form-label">Имя пользователя</label>
          <input
            id="name"
            v-model="name"
            type="text"
            class="form-input"
            :class="{ 'form-input-error': errors.name }"
            placeholder="Введите имя пользователя"
          />
          <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
        </div>

        <div class="form-group">
          <label for="password" class="form-label">Пароль</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="form-input"
            :class="{ 'form-input-error': errors.password }"
            placeholder="Введите пароль"
          />
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>

        <div v-if="submitError" class="error-message error-message-global">
          {{ submitError }}
        </div>

        <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? 'Вход...' : 'Войти' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>
          Нет аккаунта?
          <router-link to="/register" class="auth-link">Зарегистрироваться</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const name = ref('')
const password = ref('')
const isSubmitting = ref(false)
const submitError = ref('')

const errors = reactive({
  name: '',
  password: '',
})

/**
 * Валидация имени пользователя согласно AuthenticationRequest DTO
 */
const validateName = () => {
  errors.name = ''

  if (!name.value || name.value.trim() === '') {
    errors.name = 'Имя не должно быть пустым'
    return false
  }

  const trimmedName = name.value.trim()
  if (trimmedName.length < 2 || trimmedName.length > 255) {
    errors.name = 'Минимальная длина имени должна быть 2, а максимальная 255'
    return false
  }

  return true
}

/**
 * Валидация пароля согласно AuthenticationRequest DTO
 */
const validatePassword = () => {
  errors.password = ''

  if (!password.value || password.value.trim() === '') {
    errors.password = 'Пароль не должен быть пустым'
    return false
  }

  return true
}

/**
 * Валидация всех полей формы
 */
const validateForm = () => {
  const isNameValid = validateName()
  const isPasswordValid = validatePassword()
  return isNameValid && isPasswordValid
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
    const result = await authStore.login({
      name: name.value.trim(),
      password: password.value,
    })

    if (result.success) {
      router.push('/')
    } else {
      submitError.value = result.error || 'Ошибка входа в систему'
    }
  } catch (error) {
    submitError.value = error.message || 'Произошла ошибка при входе'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  padding: 20px;
}

.auth-card {
  background: #1a1a1a;
  border: 2px solid #00ff88;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0, 255, 136, 0.1);
}

.auth-title {
  color: #00ff88;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 30px;
  text-align: center;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.form-input {
  padding: 12px 16px;
  background: #0a0a0a;
  border: 1px solid #333;
  border-radius: 8px;
  color: #e0e0e0;
  font-size: 16px;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0, 255, 136, 0.1);
}

.form-input::placeholder {
  color: #666;
}

.form-input-error {
  border-color: #ff4444;
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

.btn {
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
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

.auth-footer {
  margin-top: 24px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.auth-link {
  color: #00ff88;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.auth-link:hover {
  color: #00cc6a;
  text-decoration: underline;
}
</style>
