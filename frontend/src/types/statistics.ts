/**
 * 统计分析模块 - 类型定义
 */

/** 概览统计 */
export interface OverviewStats {
  totalEvents: number
  totalParticipants: number
  totalRegistrations: number
  totalResults: number
}

/** 首页统计 */
export interface DashboardStats {
  totalEvents: number
  ongoingEvents: number
  totalRegistrations: number
  pendingRegistrations: number
  totalResults: number
}

/** 赛事报名人数排行项 */
export interface RegistrationTopItem {
  eventId: number
  eventName: string
  count: number
}

/** 成绩分布项 */
export interface ScoreDistributionItem {
  rankRange: string
  count: number
}

/** 月度趋势项 */
export interface MonthlyTrendItem {
  month: string
  eventCount: number
  registrationCount: number
  participantCount: number
}

/** 赛事类型分布项 */
export interface TypeDistributionItem {
  type: string
  typeName: string
  count: number
}

/** 赛事状态统计项 */
export interface EventStatusItem {
  status: number
  statusName: string
  count: number
}
