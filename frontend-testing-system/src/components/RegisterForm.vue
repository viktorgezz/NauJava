<!--
  Компонент формы регистрации нового пользователя.
  Реализует валидацию полей согласно требованиям бэкенда (RegistrationRequest DTO)
  включая проверку совпадения паролей и интеграцию с Pinia store.
-->
<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">Регистрация</h2>

      <form @submit.prevent="handleSubmit" class="auth-form">
        <div class="form-group">
          <label for="username" class="form-label">Имя пользователя</label>
          <input
            id="username"
            v-model="username"
            type="text"
            class="form-input"
            :class="{ 'form-input-error': errors.username }"
            placeholder="Введите имя пользователя"
          />
          <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
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

        <div class="form-group">
          <label for="confirmPassword" class="form-label">Подтверждение пароля</label>
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            class="form-input"
            :class="{ 'form-input-error': errors.confirmPassword }"
            placeholder="Повторите пароль"
          />
          <span v-if="errors.confirmPassword" class="error-message">{{
            errors.confirmPassword
          }}</span>
        </div>

        <div v-if="submitError" class="error-message error-message-global">
          {{ submitError }}
        </div>

        <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? 'Регистрация...' : 'Зарегистрироваться' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>
          Уже есть аккаунт?
          <router-link to="/login" class="auth-link">Войти</router-link>
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

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const submitError = ref('')

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

/**
 * Валидация имени пользователя согласно RegistrationRequest DTO
 */
const validateUsername = () => {
  errors.username = ''

  if (!username.value || username.value.trim() === '') {
    errors.username = 'Имя не должно быть пустым'
    return false
  }

  const trimmedUsername = username.value.trim()
  if (trimmedUsername.length < 2 || trimmedUsername.length > 255) {
    errors.username = 'Минимальная длина имени должна быть 2, а максимальная 255'
    return false
  }

  return true
}

/**
 * Валидация пароля согласно RegistrationRequest DTO
 */
const validatePassword = () => {
  errors.password = ''

  if (!password.value || password.value.trim() === '') {
    errors.password = 'Пароль не должен быть пустым'
    return false
  }

  if (password.value.length < 8 || password.value.length > 255) {
    errors.password = 'Минимальная длина пароля должна быть 8, а максимальная 255'
    return false
  }

  return true
}

/**
 * Валидация подтверждения пароля
 */
const validateConfirmPassword = () => {
  errors.confirmPassword = ''

  if (!confirmPassword.value || confirmPassword.value.trim() === '') {
    errors.confirmPassword = 'Повторный пароль не должен быть пустым'
    return false
  }

  if (confirmPassword.value.length < 8 || confirmPassword.value.length > 255) {
    errors.confirmPassword = 'Минимальная длина повторного пароля должна быть 8, а максимальная 255'
    return false
  }

  if (password.value !== confirmPassword.value) {
    errors.confirmPassword = 'Пароли не совпадают'
    return false
  }

  return true
}

/**
 * Валидация всех полей формы
 */
const validateForm = () => {
  const isUsernameValid = validateUsername()
  const isPasswordValid = validatePassword()
  const isConfirmPasswordValid = validateConfirmPassword()
  return isUsernameValid && isPasswordValid && isConfirmPasswordValid
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
    const result = await authStore.register({
      username: username.value.trim(),
      password: password.value,
      confirmPassword: confirmPassword.value,
    })

    if (result.success) {
      router.push('/login')
    } else {
      submitError.value = result.error || 'Ошибка регистрации'
    }
  } catch (error) {
    submitError.value = error.message || 'Произошла ошибка при регистрации'
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
