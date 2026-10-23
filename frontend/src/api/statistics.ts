/**
 * 统计分析模块 - API
 */
import { get } from '@/utils/request'
import type {
  OverviewStats,
  DashboardStats,
  RegistrationTopItem,
  ScoreDistributionItem,
  MonthlyTrendItem,
  TypeDistributionItem,
  EventStatusItem
} from '@/types/statistics'

/** 获取概览统计数据 */
export function getOverviewStats() {
  return get<OverviewStats>('/statistics/overview')
}

/** 获取首页统计数据 */
export function getDashboardStats() {
  return get<DashboardStats>('/statistics/dashboard')
}

/** 获取赛事报名人数 Top10 */
export function getRegistrationTop() {
  return get<RegistrationTopItem[]>('/statistics/registration-top')
}

/** 获取成绩分布统计 */
export function getScoreDistribution(eventId: number) {
  return get<ScoreDistributionItem[]>('/statistics/score-distribution', { eventId })
}

/** 获取月度趋势统计 */
export function getMonthlyTrend() {
  return get<MonthlyTrendItem[]>('/statistics/monthly-trend')
}

/** 获取赛事类型分布统计 */
export function getTypeDistribution() {
  return get<TypeDistributionItem[]>('/statistics/type-distribution')
}

/** 获取赛事状态统计 */
export function getEventStatusStats() {
  return get<EventStatusItem[]>('/statistics/event-status')
}
