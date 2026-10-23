/**
 * 资讯（新闻/公告）管理模块 - 类型定义
 */

/** 资讯 VO */
export interface News {
  id: number
  title: string
  /** ANNOUNCEMENT-公告 NEWS-新闻 NOTICE-通知 */
  type: string
  typeName?: string
  content: string
  coverImage?: string
  eventId?: number
  eventName?: string
  /** 0-否 1-是 */
  isTop: number
  viewCount: number
  publishTime?: string
  /** 0-草稿 1-已发布 2-已下架 */
  status: number
  statusName?: string
  createBy?: number
  authorName?: string
  createTime?: string
}

/** 创建/更新资讯 DTO */
export interface NewsDTO {
  title: string
  type: string
  content: string
  coverImage?: string
  eventId?: number | null
  isTop?: number
  status?: number
}

/** 资讯分页查询参数 */
export interface NewsQuery {
  current?: number
  size?: number
  title?: string
  type?: string
  status?: number | null
}
