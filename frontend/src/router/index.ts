import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 赛事中心（学生也可浏览，管理按钮按角色隐藏）
      {
        path: 'events',
        name: 'Events',
        component: () => import('@/views/event/list.vue'),
        meta: { title: '赛事中心', icon: 'Trophy' }
      },
      {
        path: 'events/:id',
        name: 'EventDetail',
        component: () => import('@/views/event/detail.vue'),
        meta: { title: '赛事详情', hidden: true }
      },
      // 运动项目
      {
        path: 'sport-items',
        name: 'SportItems',
        component: () => import('@/views/sportitem/list.vue'),
        meta: { title: '运动项目', icon: 'Football', roles: ['ADMIN', 'TEACHER'] }
      },
      // 报名管理
      {
        path: 'registrations',
        name: 'Registrations',
        component: () => import('@/views/registration/list.vue'),
        meta: { title: '报名管理', icon: 'Document', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/registration/my.vue'),
        meta: { title: '我的报名', icon: 'Tickets', roles: ['STUDENT'] }
      },
      // 赛程管理
      {
        path: 'schedules',
        name: 'Schedules',
        component: () => import('@/views/schedule/list.vue'),
        meta: { title: '赛程安排', icon: 'Calendar' }
      },
      // 成绩管理
      {
        path: 'results',
        name: 'Results',
        component: () => import('@/views/result/list.vue'),
        meta: { title: '成绩管理', icon: 'Medal', roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'my-results',
        name: 'MyResults',
        component: () => import('@/views/result/my.vue'),
        meta: { title: '我的成绩', icon: 'DataLine', roles: ['STUDENT'] }
      },
      // 新闻公告
      {
        path: 'news',
        name: 'News',
        component: () => import('@/views/news/list.vue'),
        meta: { title: '新闻公告', icon: 'Bell' }
      },
      // 用户管理
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      // 统计分析
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis', roles: ['ADMIN', 'TEACHER'] }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled', hidden: true }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无访问权限', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '校园体育赛事管理系统'} - 校园体育赛事管理系统`

  const userStore = useUserStore()
  const token = userStore.token

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 未登录跳转登录页
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 刷新后内存中的用户信息会丢失，先从后端补齐，避免菜单与权限判断失效
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      userStore.logout()
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 检查角色权限，越权访问统一跳转到 403 页面
  const roles = to.meta.roles as string[] | undefined
  if (roles && roles.length > 0) {
    const userRole = userStore.userInfo?.role
    if (!userRole || !roles.includes(userRole)) {
      next({ path: '/403', query: { from: to.fullPath } })
      return
    }
  }

  next()
})

export default router