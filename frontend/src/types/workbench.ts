export interface WorkbenchSchedule {
  id: number
  eventId: number
  eventName: string
  sportItemId: number
  sportItemName: string
  scheduleName: string
  matchDate: string
  startTime: string
  endTime: string
  location: string
  referee: string
  status: number
  statusName: string
}

export interface WorkbenchRegistration {
  id: number
  eventId: number
  eventName: string
  sportItemId: number
  sportItemName: string
  realName: string
  studentNo: string
  phone: string
  status: number
  statusName: string
  createTime: string
}

export interface WorkbenchEvent {
  id: number
  name: string
  typeName: string
  startDate: string
  endDate: string
  regEndDate: string
  location: string
  statusName: string
  registrationCount: number
  sportItemCount: number
}

export interface WorkbenchResult {
  id: number
  eventId: number
  eventName: string
  sportItemName: string
  score: number | null
  scoreText: string
  ranking: number | null
  points: number | null
  unit: string
  recordTime: string
}

export interface WorkbenchNews {
  id: number
  title: string
  type: string
  typeName: string
  publishTime: string
  isTop: number
}

/** 工作台汇总接口返回结构 */
export interface WorkbenchSummary {
  role: string
  roleName: string
  today: string
  cards: Record<string, number>
  todaySchedules?: WorkbenchSchedule[]
  recentPendingRegistrations?: WorkbenchRegistration[]
  availableEvents?: WorkbenchEvent[]
  myRecentRegistrations?: WorkbenchRegistration[]
  myRecentResults?: WorkbenchResult[]
  recentNews?: WorkbenchNews[]
}
