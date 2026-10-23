/**
 * 资讯（新闻/公告）管理模块 - API
 */
import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types/auth'
import type { News, NewsDTO, NewsQuery } from '@/types/news'

/** 分页查询资讯 */
export function pageNews(params: NewsQuery) {
  return get<PageResult<News>>('/news', params)
}

/** 获取资讯详情 */
export function getNewsDetail(id: number) {
  return get<News>(`/news/${id}`)
}

/** 创建资讯 */
export function createNews(data: NewsDTO) {
  return post<number>('/news', data)
}

/** 更新资讯 */
export function updateNews(id: number, data: NewsDTO) {
  return put<void>(`/news/${id}`, data)
}

/** 删除资讯 */
export function deleteNews(id: number) {
  return del<void>(`/news/${id}`)
}

/** 发布资讯 */
export function publishNews(id: number) {
  return post<void>(`/news/${id}/publish`)
}

/** 下架资讯 */
export function offlineNews(id: number) {
  return post<void>(`/news/${id}/offline`)
}
