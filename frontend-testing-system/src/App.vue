<!--
  Корневой компонент приложения.
  Обеспечивает базовую структуру и роутинг для всего приложения.
-->
<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  authStore.initialize()
})
</script>

<template>
  <router-view />
</template>

<style>
@import '@/assets/styles/global.css';
</style>
