/**
 * 赛事管理模块 - API
 */
import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types/auth'
import type { Event, EventDTO, EventQuery } from '@/types/event'

/** 分页查询赛事 */
export function pageEvents(params: EventQuery) {
  return get<PageResult<Event>>('/events', params)
}

/** 获取赛事详情 */
export function getEventDetail(id: number) {
  return get<Event>(`/events/${id}`)
}

/** 创建赛事 */
export function createEvent(data: EventDTO) {
  return post<number>('/events', data)
}

/** 更新赛事 */
export function updateEvent(id: number, data: EventDTO) {
  return put<void>(`/events/${id}`, data)
}

/** 删除赛事 */
export function deleteEvent(id: number) {
  return del<void>(`/events/${id}`)
}

/** 发布赛事 */
export function publishEvent(id: number) {
  return post<void>(`/events/${id}/publish`)
}

/** 取消赛事 */
export function cancelEvent(id: number) {
  return post<void>(`/events/${id}/cancel`)
}

/** 获取可报名的赛事列表 */
export function getAvailableEvents() {
  return get<Event[]>('/events/available')
}

/** 获取进行中的赛事列表 */
export function getOngoingEvents() {
  return get<Event[]>('/events/ongoing')
}
