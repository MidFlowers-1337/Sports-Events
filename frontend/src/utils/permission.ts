import { useUserStore } from '@/stores/user'

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

/** 角色显示名 */
export const ROLE_LABELS: Record<string, string> = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生'
}

/** 当前登录用户角色 */
export function currentRole(): string {
  return useUserStore().userInfo?.role || ''
}

/**
 * 判断当前用户是否具备指定角色
 * 传空数组视为不限制角色，直接放行
 */
export function hasRole(required?: string | string[] | null): boolean {
  if (required === undefined || required === null) {
    return true
  }
  const roles = Array.isArray(required) ? required : [required]
  if (roles.length === 0) {
    return true
  }
  return roles.includes(currentRole())
}

/** 是否为管理员 */
export function isAdmin(): boolean {
  return currentRole() === 'ADMIN'
}

/** 是否为教师 */
export function isTeacher(): boolean {
  return currentRole() === 'TEACHER'
}

/** 是否为学生 */
export function isStudent(): boolean {
  return currentRole() === 'STUDENT'
}

/** 是否具备管理类角色（管理员或教师） */
export function isManager(): boolean {
  return isAdmin() || isTeacher()
}
