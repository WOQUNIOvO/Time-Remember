import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/calendar' },
    { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: 'calendar', component: () => import('@/views/CalendarView.vue') },
        { path: 'categories', component: () => import('@/views/CategoryView.vue') },
        { path: 'statistics', component: () => import('@/views/StatisticsView.vue') }
      ]
    }
  ]
})

router.beforeEach(to => {
  if (!to.meta.public && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/calendar'
  }
  return true
})

export default router
