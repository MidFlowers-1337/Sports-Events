/**
 * 运动项目管理模块 - API
 */
import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types/auth'
import type { SportItem, SportItemDTO, SportItemQuery } from '@/types/sportitem'

/** 分页查询运动项目 */
export function pageSportItems(params: SportItemQuery) {
  return get<PageResult<SportItem>>('/sport-items', params)
}

/** 根据赛事ID获取项目列表 */
export function getSportItemsByEvent(eventId: number) {
  return get<SportItem[]>(`/sport-items/event/${eventId}`)
}

/** 获取项目详情 */
export function getSportItemDetail(id: number) {
  return get<SportItem>(`/sport-items/${id}`)
}

/** 创建运动项目 */
export function createSportItem(data: SportItemDTO) {
  return post<number>('/sport-items', data)
}

/** 更新运动项目 */
export function updateSportItem(id: number, data: SportItemDTO) {
  return put<void>(`/sport-items/${id}`, data)
}

/** 删除运动项目 */
export function deleteSportItem(id: number) {
  return del<void>(`/sport-items/${id}`)
}
