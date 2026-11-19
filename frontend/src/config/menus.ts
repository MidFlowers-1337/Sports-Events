import type { UserRole } from '@/utils/permission'

/** 单个菜单项 */
export interface MenuItem {
  path: string
  title: string
  icon: string
}

/** 带子菜单的分组 */
export interface MenuGroup {
  title: string
  icon: string
  children: MenuItem[]
}

export type MenuNode = MenuItem | MenuGroup

/** 判断节点是否为分组 */
export function isMenuGroup(node: MenuNode): node is MenuGroup {
  return 'children' in node
}

/**
 * 三类角色各自的侧边栏菜单
 * 路径必须与 router/index.ts 中已注册的路由保持一致
 */
export const ROLE_MENUS: Record<UserRole, MenuNode[]> = {
  ADMIN: [
    { path: '/dashboard', title: '管理工作台', icon: 'Odometer' },
    {
      title: '赛事管理',
      icon: 'Trophy',
      children: [
        { path: '/events', title: '赛事列表', icon: 'List' },
        { path: '/sport-items', title: '运动项目', icon: 'Football' },
        { path: '/schedules', title: '赛程编排', icon: 'Calendar' }
      ]
    },
    { path: '/registrations', title: '报名审核', icon: 'DocumentChecked' },
    { path: '/results', title: '成绩管理', icon: 'Medal' },
    { path: '/statistics', title: '统计分析', icon: 'DataAnalysis' },
    { path: '/news', title: '公告管理', icon: 'Bell' },
    { path: '/users', title: '用户管理', icon: 'User' }
  ],
  TEACHER: [
    { path: '/dashboard', title: '教学工作台', icon: 'Odometer' },
    { path: '/events', title: '赛事查看', icon: 'Trophy' },
    { path: '/registrations', title: '报名审核', icon: 'DocumentChecked' },
    { path: '/schedules', title: '赛程查看', icon: 'Calendar' },
    { path: '/results', title: '成绩录入', icon: 'Medal' },
    { path: '/statistics', title: '统计分析', icon: 'DataAnalysis' },
    { path: '/news', title: '公告通知', icon: 'Bell' }
  ],
  STUDENT: [
    { path: '/dashboard', title: '我的工作台', icon: 'Odometer' },
    { path: '/events', title: '赛事大厅', icon: 'Trophy' },
    { path: '/my-registrations', title: '我的报名', icon: 'Tickets' },
    { path: '/my-results', title: '我的成绩', icon: 'DataLine' },
    { path: '/schedules', title: '赛程安排', icon: 'Calendar' },
    { path: '/news', title: '公告通知', icon: 'Bell' }
  ]
}

/** 工作台标题与说明，用于侧边栏与首页横幅 */
export const ROLE_WORKBENCH: Record<UserRole, { title: string; desc: string }> = {
  ADMIN: {
    title: '管理工作台',
    desc: '查看全站赛事、报名与成绩数据，处理待审核事项'
  },
  TEACHER: {
    title: '教学工作台',
    desc: '处理报名审核、查看今日赛程、录入比赛成绩'
  },
  STUDENT: {
    title: '我的工作台',
    desc: '浏览可报名赛事，跟踪报名进度与个人成绩'
  }
}

/** 按角色取菜单，未识别的角色返回空菜单 */
export function getMenusByRole(role?: string): MenuNode[] {
  if (!role || !(role in ROLE_MENUS)) {
    return []
  }
  return ROLE_MENUS[role as UserRole]
}

/** 按角色取工作台文案 */
export function getWorkbenchMeta(role?: string): { title: string; desc: string } {
  if (role && role in ROLE_WORKBENCH) {
    return ROLE_WORKBENCH[role as UserRole]
  }
  return { title: '工作台', desc: '' }
}
