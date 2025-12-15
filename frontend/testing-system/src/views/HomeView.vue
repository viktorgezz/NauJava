<!--
  Главная страница приложения для авторизованных пользователей.
  Отображает список тестов с пагинацией и поиском.
-->
<template>
  <div class="home-container">
    <div class="home-header">
      <div class="home-header-content">
        <h1 class="home-title">Система тестирования</h1>
        <div class="header-actions">
          <button @click="openCreateModal" class="btn btn-create">Создать тест</button>
          <button @click="goToProfile" class="btn btn-profile">Профиль</button>
        </div>
      </div>
    </div>

    <TestList ref="testListRef" />

    <CreateTestModal
      :is-open="isCreateModalOpen"
      @close="closeCreateModal"
      @success="handleTestCreated"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import TestList from '@/components/TestList.vue'
import CreateTestModal from '@/components/CreateTestModal.vue'

const router = useRouter()
const testListRef = ref(null)

const isCreateModalOpen = ref(false)

/**
 * Открытие модального окна создания теста
 */
const openCreateModal = () => {
  isCreateModalOpen.value = true
}

/**
 * Закрытие модального окна создания теста
 */
const closeCreateModal = () => {
  isCreateModalOpen.value = false
}

/**
 * Обработка успешного создания теста
 */
const handleTestCreated = () => {
  closeCreateModal()
  // Обновляем список тестов
  if (testListRef.value) {
    testListRef.value.refresh()
  }
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
}

.home-header {
  background: #1a1a1a;
  border-bottom: 2px solid #333;
  padding: 20px;
  margin-bottom: 0;
}

.home-header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home-title {
  color: #00ff88;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-create {
  background: #00ff88;
  color: #0a0a0a;
}

.btn-create:hover {
  background: #00cc6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}

.btn-profile {
  background: transparent;
  color: #00ff88;
  border: 2px solid #00ff88;
}

.btn-profile:hover {
  background: #00ff88;
  color: #0a0a0a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.3);
}
</style>
