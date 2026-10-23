<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon size="32"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalEvents }}</div>
            <div class="stat-label">赛事总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalParticipants }}</div>
            <div class="stat-label">参赛人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalRegistrations }}</div>
            <div class="stat-label">报名总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon size="32"><Medal /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalResults }}</div>
            <div class="stat-label">成绩记录</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 赛事报名Top10 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>赛事报名人数 Top10</span>
          </template>
          <div ref="registrationChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 成绩分布 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>成绩分布</span>
              <el-select v-model="selectedEventId" placeholder="选择赛事" size="small" @change="loadScoreDistribution">
                <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
          </template>
          <div ref="scoreChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 月度趋势 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>月度赛事趋势</span>
          </template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>

      <!-- 赛事类型分布 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>赛事类型分布</span>
          </template>
          <div ref="typeChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { pageEvents } from '@/api/event'
import { getOverviewStats, getRegistrationTop, getScoreDistribution, getMonthlyTrend, getTypeDistribution } from '@/api/statistics'
import type { Event } from '@/types/event'
import type { OverviewStats } from '@/types/statistics'

const registrationChartRef = ref<HTMLElement>()
const scoreChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()

let registrationChart: echarts.ECharts | null = null
let scoreChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null

const eventOptions = ref<Event[]>([])
const selectedEventId = ref<number | null>(null)

const overview = reactive<OverviewStats>({
  totalEvents: 0,
  totalParticipants: 0,
  totalRegistrations: 0,
  totalResults: 0
})

onMounted(async () => {
  await loadEventOptions()
  await loadOverview()
  initCharts()
  await loadAllChartData()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  registrationChart?.dispose()
  scoreChart?.dispose()
  trendChart?.dispose()
  typeChart?.dispose()
})

function handleResize() {
  registrationChart?.resize()
  scoreChart?.resize()
  trendChart?.resize()
  typeChart?.resize()
}

async function loadEventOptions() {
  const res = await pageEvents({ size: 100 })
  if (res.code === 200) {
    eventOptions.value = res.data.records
    if (eventOptions.value.length > 0) {
      selectedEventId.value = eventOptions.value[0].id
    }
  }
}

async function loadOverview() {
  const res = await getOverviewStats()
  if (res.code === 200) {
    Object.assign(overview, res.data)
  }
}

function initCharts() {
  if (registrationChartRef.value) {
    registrationChart = echarts.init(registrationChartRef.value)
  }
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
}

async function loadAllChartData() {
  await Promise.all([
    loadRegistrationTop(),
    loadScoreDistribution(),
    loadMonthlyTrend(),
    loadTypeDistribution()
  ])
}

async function loadRegistrationTop() {
  const res = await getRegistrationTop()
  if (res.code === 200 && registrationChart) {
    const data = res.data || []
    registrationChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: data.map((item) => item.eventName).reverse(),
        axisLabel: {
          width: 100,
          overflow: 'truncate'
        }
      },
      series: [{
        name: '报名人数',
        type: 'bar',
        data: data.map((item) => item.count).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }]
    })
  }
}

async function loadScoreDistribution() {
  if (!selectedEventId.value) return
  const res = await getScoreDistribution(selectedEventId.value)
  if (res.code === 200 && scoreChart) {
    const data = res.data || []
    scoreChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        name: '名次分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        data: data.map((item) => ({
          name: item.rankRange,
          value: item.count
        }))
      }]
    })
  }
}

async function loadMonthlyTrend() {
  const res = await getMonthlyTrend()
  if (res.code === 200 && trendChart) {
    const data = res.data || []
    trendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['赛事数', '报名数', '参赛人数']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.map((item) => item.month)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '赛事数',
          type: 'line',
          smooth: true,
          data: data.map((item) => item.eventCount),
          areaStyle: { opacity: 0.3 }
        },
        {
          name: '报名数',
          type: 'line',
          smooth: true,
          data: data.map((item) => item.registrationCount),
          areaStyle: { opacity: 0.3 }
        },
        {
          name: '参赛人数',
          type: 'line',
          smooth: true,
          data: data.map((item) => item.participantCount),
          areaStyle: { opacity: 0.3 }
        }
      ]
    })
  }
}

async function loadTypeDistribution() {
  const res = await getTypeDistribution()
  if (res.code === 200 && typeChart) {
    const data = res.data || []
    typeChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      series: [{
        name: '赛事类型',
        type: 'pie',
        radius: '70%',
        data: data.map((item) => ({
          name: item.typeName,
          value: item.count
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
