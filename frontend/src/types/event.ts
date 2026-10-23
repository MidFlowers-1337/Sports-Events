/**
 * 赛事管理模块 - 类型定义
 */

/** 赛事状态：1-草稿 2-报名中 3-进行中 4-已结束 */
export type EventStatus = 1 | 2 | 3 | 4

/** 赛事 VO */
export interface Event {
  id: number
  name: string
  description?: string
  type: string
  typeName?: string
  startDate: string
  endDate: string
  regStartDate?: string
  regEndDate?: string
  location?: string
  organizer?: string
  contactPhone?: string
  coverImage?: string
  status: EventStatus
  statusName?: string
  registrationCount?: number
  sportItemCount?: number
  createTime?: string
}

/** 创建/更新赛事 DTO */
export interface EventDTO {
  name: string
  description?: string
  type: string
  startDate: string
  endDate: string
  regStartDate: string
  regEndDate: string
  location?: string
  organizer?: string
  contactPhone?: string
  coverImage?: string
  status?: EventStatus
}

/** 赛事分页查询参数 */
export interface EventQuery {
  current?: number
  size?: number
  name?: string
  type?: string
  status?: EventStatus | null
  year?: number
}
