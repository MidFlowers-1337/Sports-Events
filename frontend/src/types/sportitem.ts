/**
 * 运动项目管理模块 - 类型定义
 */

/** 运动项目 */
export interface SportItem {
  id: number
  eventId: number
  name: string
  /** TRACK-径赛 FIELD-田赛 BALL-球类 SWIMMING-游泳 OTHER-其他 */
  type: string
  /** 0-不限 1-仅男 2-仅女 */
  genderLimit: number
  maxParticipants: number
  currentParticipants: number
  rules?: string
  /** TIME-计时 DISTANCE-计距离 SCORE-计分 */
  scoreType?: string
  scoreUnit?: string
  /** 0-禁用 1-启用 */
  status: number
}

/** 创建/更新运动项目 DTO */
export interface SportItemDTO {
  eventId: number
  name: string
  type: string
  genderLimit: number
  maxParticipants: number
  rules?: string
  scoreType?: string
  scoreUnit?: string
  status?: number
}

/** 运动项目分页查询参数 */
export interface SportItemQuery {
  current?: number
  size?: number
  eventId?: number | null
  name?: string
  type?: string
  status?: number | null
}
