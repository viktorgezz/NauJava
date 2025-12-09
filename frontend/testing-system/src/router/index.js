/**
 * Конфигурация роутера Vue Router.
 * Определяет маршруты приложения и защиту приватных маршрутов через навигационные хуки.
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/components/LoginForm.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/components/RegisterForm.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/tests/:id/edit',
      name: 'test-content-editor',
      component: () => import('@/views/TestContentEditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/tests/:id/pass',
      name: 'test-passing',
      component: () => import('@/views/TestPassingView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/results/:id',
      name: 'test-result',
      component: () => import('@/views/TestResultView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

/**
 * Навигационный хук для защиты маршрутов
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // Инициализация store при первой загрузке
  if (!authStore.getAccessToken()) {
    authStore.initialize()
  }

  const isAuthenticated = authStore.isAuthenticated

  // Если маршрут требует аутентификации
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  // Если маршрут доступен только для гостей (неавторизованных)
  if (to.meta.requiresGuest && isAuthenticated) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
