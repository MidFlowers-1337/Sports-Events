<template>
  <div class="wb">
    <section class="wb-hero">
      <div class="wb-hero__head">
        <div>
          <div class="wb-hero__title">{{ meta.title }}</div>
          <div class="wb-hero__desc">{{ studentName }}，{{ meta.desc }}</div>
        </div>
        <div class="wb-hero__date">
          <el-icon><Calendar /></el-icon>
          <span>{{ todayText }}</span>
        </div>
      </div>
      <div class="wb-hero__actions">
        <el-button round @click="router.push('/events')">赛事大厅</el-button>
        <el-button round @click="router.push('/my-registrations')">我的报名</el-button>
        <el-button round @click="router.push('/my-results')">我的成绩</el-button>
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
          <span class="wb-card__title">可报名赛事</span>
          <el-button link type="primary" @click="router.push('/events')">查看全部</el-button>
        </div>
        <div class="wb-card__body">
          <div v-if="availableEvents.length" class="wb-event-list">
            <div
              v-for="event in availableEvents"
              :key="event.id"
              class="wb-event"
              @click="router.push(`/events/${event.id}`)"
            >
              <div class="wb-event__name">{{ event.name }}</div>
              <div class="wb-event__meta">
                <el-icon><Calendar /></el-icon>
                {{ event.startDate }} 至 {{ event.endDate }}
              </div>
              <div class="wb-event__meta">
                <el-icon><Location /></el-icon>
                {{ event.location || '地点待定' }}
                <el-tag size="small" effect="plain" type="success">报名截止 {{ event.regEndDate || '未设置' }}</el-tag>
              </div>
            </div>
          </div>
          <div v-else class="wb-empty">当前没有开放的报名赛事</div>
        </div>
      </div>

      <div class="wb-stack">
        <div class="wb-card">
          <div class="wb-card__head">
            <span class="wb-card__title">我的奖牌</span>
          </div>
          <div class="wb-card__body">
            <div class="wb-medals">
              <div class="wb-medal"><span class="wb-medal__num">{{ cards.goldMedals ?? 0 }}</span> 金牌</div>
              <div class="wb-medal"><span class="wb-medal__num">{{ cards.silverMedals ?? 0 }}</span> 银牌</div>
              <div class="wb-medal"><span class="wb-medal__num">{{ cards.bronzeMedals ?? 0 }}</span> 铜牌</div>
            </div>
          </div>
        </div>

        <div class="wb-card">
          <div class="wb-card__head">
            <span class="wb-card__title">最新公告</span>
            <el-button link type="primary" @click="router.push('/news')">全部公告</el-button>
          </div>
          <div class="wb-card__body">
            <div v-if="recentNews.length" class="wb-list">
              <div v-for="news in recentNews" :key="news.id" class="wb-list__item" @click="router.push('/news')">
                <div class="wb-list__main">
                  <div class="wb-list__title">{{ news.title }}</div>
                  <div class="wb-list__sub">{{ news.typeName }} | {{ formatDate(news.publishTime) }}</div>
                </div>
                <el-tag v-if="news.isTop === 1" size="small" type="danger" effect="plain">置顶</el-tag>
              </div>
            </div>
            <div v-else class="wb-empty">暂无公告</div>
          </div>
        </div>
      </div>
    </div>

    <div class="wb-cols">
      <div class="wb-card">
        <div class="wb-card__head">
          <span class="wb-card__title">最近报名</span>
          <el-button link type="primary" @click="router.push('/my-registrations')">全部报名</el-button>
        </div>
        <div class="wb-card__body wb-card__body--flush">
          <el-table :data="myRegistrations" size="small" :show-header="myRegistrations.length > 0">
            <el-table-column prop="eventName" label="赛事" min-width="150" show-overflow-tooltip />
            <el-table-column prop="sportItemName" label="项目" width="120" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="regTagType(row.status)">{{ row.statusName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="报名时间" width="165" />
          </el-table>
        </div>
      </div>

      <div class="wb-card">
        <div class="wb-card__head">
          <span class="wb-card__title">最近成绩</span>
          <el-button link type="primary" @click="router.push('/my-results')">全部成绩</el-button>
        </div>
        <div class="wb-card__body">
          <div v-if="myResults.length" class="wb-list">
            <div v-for="result in myResults" :key="result.id" class="wb-list__item">
              <div class="wb-list__main">
                <div class="wb-list__title">{{ result.eventName }} · {{ result.sportItemName }}</div>
                <div class="wb-list__sub">
                  成绩 {{ result.scoreText || result.score || '-' }} | 记录时间 {{ formatDate(result.recordTime) }}
                </div>
              </div>
              <el-tag v-if="result.ranking" size="small" type="warning" effect="plain">第 {{ result.ranking }} 名</el-tag>
              <el-tag v-else size="small" effect="plain">待排名</el-tag>
            </div>
          </div>
          <div v-else class="wb-empty">还没有比赛成绩，先报名参加一场比赛吧</div>
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
import type {
  WorkbenchEvent,
  WorkbenchNews,
  WorkbenchRegistration,
  WorkbenchResult,
  WorkbenchSummary
} from '@/types/workbench'

const router = useRouter()
const userStore = useUserStore()

const meta = getWorkbenchMeta('STUDENT')
const todayText = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const summary = ref<WorkbenchSummary | null>(null)

const studentName = computed(() => userStore.userInfo?.realName || '同学')
const cards = computed<Record<string, number>>(() => summary.value?.cards || {})
const availableEvents = computed<WorkbenchEvent[]>(() => summary.value?.availableEvents || [])
const recentNews = computed<WorkbenchNews[]>(() => summary.value?.recentNews || [])
const myRegistrations = computed<WorkbenchRegistration[]>(
  () => summary.value?.myRecentRegistrations || []
)
const myResults = computed<WorkbenchResult[]>(() => summary.value?.myRecentResults || [])

const statCards = computed(() => [
  { label: '我的报名', value: cards.value.myRegistrations ?? 0, icon: 'Tickets', color: 'wb-icon--blue' },
  { label: '已通过', value: cards.value.approvedRegistrations ?? 0, icon: 'CircleCheck', color: 'wb-icon--green' },
  { label: '待审核', value: cards.value.pendingRegistrations ?? 0, icon: 'Clock', color: 'wb-icon--amber' },
  { label: '我的成绩', value: cards.value.myResults ?? 0, icon: 'DataLine', color: 'wb-icon--purple' }
])

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

function formatDate(value?: string) {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 10)
}

function regTagType(status: number) {
  switch (status) {
    case 2:
      return 'success'
    case 3:
      return 'danger'
    case 4:
      return 'info'
    default:
      return 'warning'
  }
}
</script>
