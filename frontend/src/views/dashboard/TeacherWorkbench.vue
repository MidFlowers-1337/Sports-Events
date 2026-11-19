<template>
  <div class="wb">
    <section class="wb-hero">
      <div class="wb-hero__head">
        <div>
          <div class="wb-hero__title">{{ meta.title }}</div>
          <div class="wb-hero__desc">{{ teacherName }}，{{ meta.desc }}</div>
        </div>
        <div class="wb-hero__date">
          <el-icon><Calendar /></el-icon>
          <span>{{ todayText }}</span>
        </div>
      </div>
      <div class="wb-hero__actions">
        <el-button round @click="router.push('/registrations')">审核报名</el-button>
        <el-button round @click="router.push('/results')">录入成绩</el-button>
        <el-button round @click="router.push('/schedules')">查看赛程</el-button>
      </div>
    </section>

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
            <el-table-column prop="studentNo" label="学号" width="120" />
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
                  <div class="wb-list__sub">
                    {{ item.scheduleName }} | {{ item.location || '场地待定' }} | 裁判 {{ item.referee || '待定' }}
                  </div>
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
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { getWorkbenchMeta } from '@/config/menus'
import type { WorkbenchRegistration, WorkbenchSchedule, WorkbenchSummary } from '@/types/workbench'

const router = useRouter()
const userStore = useUserStore()

const meta = getWorkbenchMeta('TEACHER')
const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const summary = ref<WorkbenchSummary | null>(null)

const teacherName = computed(() => userStore.userInfo?.realName || '老师')
const cards = computed<Record<string, number>>(() => summary.value?.cards || {})
const pendingList = computed<WorkbenchRegistration[]>(
  () => summary.value?.recentPendingRegistrations || []
)
const todaySchedules = computed<WorkbenchSchedule[]>(() => summary.value?.todaySchedules || [])

const statCards = computed(() => [
  { label: '待审核报名', value: cards.value.pendingRegistrations ?? 0, icon: 'DocumentChecked', color: 'wb-icon--rose' },
  { label: '今日赛程', value: cards.value.todaySchedules ?? 0, icon: 'Calendar', color: 'wb-icon--blue' },
  { label: '待录入成绩', value: cards.value.pendingResults ?? 0, icon: 'EditPen', color: 'wb-icon--amber' },
  { label: '成绩记录', value: cards.value.totalResults ?? 0, icon: 'Medal', color: 'wb-icon--green' }
])

const quickEntries = [
  { label: '报名审核', path: '/registrations', icon: 'DocumentChecked', color: 'wb-icon--rose' },
  { label: '成绩录入', path: '/results', icon: 'Medal', color: 'wb-icon--amber' },
  { label: '赛程查看', path: '/schedules', icon: 'Calendar', color: 'wb-icon--purple' },
  { label: '赛事查看', path: '/events', icon: 'Trophy', color: 'wb-icon--blue' },
  { label: '统计分析', path: '/statistics', icon: 'DataAnalysis', color: 'wb-icon--green' },
  { label: '公告通知', path: '/news', icon: 'Bell', color: 'wb-icon--blue' }
]

onMounted(loadSummary)

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
</script>
