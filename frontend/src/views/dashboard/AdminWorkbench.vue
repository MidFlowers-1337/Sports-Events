<template>
  <div class="wb">
    <!-- 角色横幅 -->
    <section class="wb-hero">
      <div class="wb-hero__head">
        <div>
          <div class="wb-hero__title">{{ meta.title }}</div>
          <div class="wb-hero__desc">{{ adminName }}，{{ meta.desc }}</div>
        </div>
        <div class="wb-hero__date">
          <el-icon><Calendar /></el-icon>
          <span>{{ todayText }}</span>
        </div>
      </div>
      <div class="wb-hero__actions">
        <el-button round @click="router.push('/events')">赛事管理</el-button>
        <el-button round @click="router.push('/registrations')">报名审核</el-button>
        <el-button round @click="router.push('/statistics')">统计分析</el-button>
      </div>
    </section>

    <!-- 核心指标 -->
    <div class="wb-stats">
      <div v-for="item in statCards" :key="item.label" class="wb-stat">
        <div class="wb-stat__icon" :class="item.color">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <div class="wb-stat__value">{{ item.value }}</div>
          <div class="wb-stat__label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="wb-cols">
      <div class="wb-card">
        <div class="wb-card__head">
          <span class="wb-card__title">赛事报名人数 Top10</span>
        </div>
        <div class="wb-card__body">
          <div ref="regChartRef" class="wb-chart"></div>
        </div>
      </div>
      <div class="wb-card">
        <div class="wb-card__head">
          <span class="wb-card__title">赛事状态分布</span>
        </div>
        <div class="wb-card__body">
          <div ref="statusChartRef" class="wb-chart"></div>
        </div>
      </div>
    </div>

    <!-- 待办与快捷入口 -->
    <div class="wb-cols">
      <div class="wb-card">
        <div class="wb-card__head">
          <span class="wb-card__title">待审核报名</span>
          <el-button link type="primary" @click="router.push('/registrations')">前往处理</el-button>
        </div>
        <div class="wb-card__body wb-card__body--flush">
          <el-table :data="pendingList" size="small" :show-header="pendingList.length > 0">
            <el-table-column prop="eventName" label="赛事" min-width="130" show-overflow-tooltip />
            <el-table-column prop="sportItemName" label="项目" width="110" show-overflow-tooltip />
            <el-table-column prop="realName" label="学生" width="90" />
            <el-table-column prop="createTime" label="提交时间" width="165" />
          </el-table>
        </div>
      </div>

      <div class="wb-stack">
        <div class="wb-card">
          <div class="wb-card__head">
            <span class="wb-card__title">今日赛程</span>
            <el-button link type="primary" @click="router.push('/schedules')">全部赛程</el-button>
          </div>
          <div class="wb-card__body">
            <div v-if="todaySchedules.length" class="wb-list">
              <div v-for="item in todaySchedules" :key="item.id" class="wb-list__item">
                <div class="wb-list__main">
                  <div class="wb-list__title">{{ item.eventName }} · {{ item.sportItemName }}</div>
                  <div class="wb-list__sub">{{ item.scheduleName }} | {{ item.location || '场地待定' }}</div>
                </div>
                <el-tag size="small" effect="plain">{{ item.startTime }}-{{ item.endTime }}</el-tag>
              </div>
            </div>
            <div v-else class="wb-empty">今日暂无赛程安排</div>
          </div>
        </div>

        <div class="wb-card">
          <div class="wb-card__head">
            <span class="wb-card__title">快捷入口</span>
          </div>
          <div class="wb-card__body">
            <div class="wb-quick">
              <div v-for="entry in quickEntries" :key="entry.path" class="wb-quick__item" @click="router.push(entry.path)">
                <div class="wb-quick__icon" :class="entry.color">
                  <el-icon><component :is="entry.icon" /></el-icon>
                </div>
                <span class="wb-quick__label">{{ entry.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { get } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { getWorkbenchMeta } from '@/config/menus'
import type { WorkbenchRegistration, WorkbenchSchedule, WorkbenchSummary } from '@/types/workbench'

const router = useRouter()
const userStore = useUserStore()

const meta = getWorkbenchMeta('ADMIN')
const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const summary = ref<WorkbenchSummary | null>(null)
const regChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()
let regChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null

const cards = computed<Record<string, number>>(() => summary.value?.cards || {})
const pendingList = computed<WorkbenchRegistration[]>(
  () => summary.value?.recentPendingRegistrations || []
)
const todaySchedules = computed<WorkbenchSchedule[]>(() => summary.value?.todaySchedules || [])

const statCards = computed(() => [
  { label: '赛事总数', value: cards.value.totalEvents ?? 0, icon: 'Trophy', color: 'wb-icon--blue' },
  { label: '进行中赛事', value: cards.value.ongoingEvents ?? 0, icon: 'VideoPlay', color: 'wb-icon--green' },
  { label: '报名总数', value: cards.value.totalRegistrations ?? 0, icon: 'Document', color: 'wb-icon--amber' },
  { label: '待审核报名', value: cards.value.pendingRegistrations ?? 0, icon: 'Bell', color: 'wb-icon--rose' }
])

const quickEntries = [
  { label: '赛事列表', path: '/events', icon: 'Trophy', color: 'wb-icon--blue' },
  { label: '运动项目', path: '/sport-items', icon: 'Football', color: 'wb-icon--green' },
  { label: '赛程编排', path: '/schedules', icon: 'Calendar', color: 'wb-icon--purple' },
  { label: '成绩管理', path: '/results', icon: 'Medal', color: 'wb-icon--amber' },
  { label: '公告管理', path: '/news', icon: 'Bell', color: 'wb-icon--rose' },
  { label: '用户管理', path: '/users', icon: 'User', color: 'wb-icon--blue' }
]

onMounted(async () => {
  await loadSummary()
  await nextTick()
  initCharts()
  await loadChartData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  regChart?.dispose()
  statusChart?.dispose()
})

function handleResize() {
  regChart?.resize()
  statusChart?.resize()
}

async function loadSummary() {
  try {
    const res = await get<WorkbenchSummary>('/workbench/summary')
    if (res.code === 200) {
      summary.value = res.data
    }
  } catch (error) {
    console.error('加载工作台数据失败', error)
  }
}

function initCharts() {
  if (regChartRef.value) {
    regChart = echarts.init(regChartRef.value)
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
}

async function loadChartData() {
  try {
    const [topRes, statusRes] = await Promise.all([
      get('/statistics/registration-top'),
      get('/statistics/event-status')
    ])
    renderTopChart(topRes.data || [])
    renderStatusChart(statusRes.data || [])
  } catch (error) {
    console.error('加载图表数据失败', error)
  }
}

function renderTopChart(data: Array<{ eventName: string; count: number }>) {
  if (!regChart) return
  regChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 12, right: 20, top: 20, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.eventName),
      axisLabel: { rotate: 28, color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: '#f1f5f9' } }
    },
    series: [
      {
        name: '报名人数',
        type: 'bar',
        barMaxWidth: 26,
        data: data.map(item => item.count),
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#7c3aed' }
          ])
        }
      }
    ]
  })
}

function renderStatusChart(data: Array<{ statusName: string; count: number }>) {
  if (!statusChart) return
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { color: '#64748b', fontSize: 12 } },
    series: [
      {
        name: '赛事状态',
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 600 } },
        data: data.map((item, index) => ({
          name: item.statusName,
          value: item.count,
          itemStyle: {
            color: ['#94a3b8', '#3b82f6', '#10b981', '#f59e0b', '#ef4444'][index % 5]
          }
        }))
      }
    ]
  })
}

// 当前登录人信息用于横幅副标题展示
const adminName = computed(() => userStore.userInfo?.realName || '管理员')
</script>
