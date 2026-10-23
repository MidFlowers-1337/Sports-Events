<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>赛事详情</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="赛事名称">{{ eventData.name }}</el-descriptions-item>
        <el-descriptions-item label="赛事类型">{{ eventData.typeName }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ eventData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ eventData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="报名开始">{{ eventData.regStartDate }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ eventData.regEndDate }}</el-descriptions-item>
        <el-descriptions-item label="举办地点">{{ eventData.location }}</el-descriptions-item>
        <el-descriptions-item label="主办单位">{{ eventData.organizer }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ eventData.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(eventData.status)">{{ eventData.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="赛事描述" :span="2">{{ eventData.description }}</el-descriptions-item>
      </el-descriptions>

      <!-- 报名按钮 -->
      <div class="mt-20" v-if="canRegister">
        <el-button type="primary" size="large" @click="handleRegister">立即报名</el-button>
      </div>
    </el-card>

    <!-- 运动项目列表 -->
    <el-card shadow="never" class="mt-20">
      <template #header>
        <span>运动项目</span>
      </template>
      <el-table :data="sportItems" border stripe>
        <el-table-column prop="name" label="项目名称" />
        <el-table-column label="类别" width="100">
          <template #default="{ row }">{{ itemTypeName(row.type) }}</template>
        </el-table-column>
        <el-table-column label="性别要求" width="100">
          <template #default="{ row }">{{ genderLimitName(row.genderLimit) }}</template>
        </el-table-column>
        <el-table-column prop="maxParticipants" label="人数上限" width="100" />
        <el-table-column prop="currentParticipants" label="已报名" width="100" />
        <el-table-column prop="rules" label="比赛规则" />
        <el-table-column label="操作" width="120" v-if="canRegister">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleRegisterItem(row)"
              :disabled="row.currentParticipants >= row.maxParticipants"
            >
              {{ row.currentParticipants >= row.maxParticipants ? '已满' : '报名' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 赛程安排 -->
    <el-card shadow="never" class="mt-20">
      <template #header>
        <span>赛程安排</span>
      </template>
      <el-timeline v-if="schedules.length > 0">
        <el-timeline-item
          v-for="item in schedules"
          :key="item.id"
          :timestamp="item.matchDate + ' ' + item.startTime"
          placement="top"
        >
          <el-card shadow="hover">
            <h4>{{ item.sportItemName }} - {{ scheduleName(item) }}</h4>
            <p>地点：{{ item.location }}</p>
            <p v-if="item.groupNo">分组：{{ item.groupNo }}</p>
            <p>状态：<el-tag :type="getScheduleStatusType(item.status)">{{ scheduleStatusName(item.status) }}</el-tag></p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无赛程安排" />
    </el-card>

    <!-- 报名弹窗 -->
    <el-dialog v-model="registerDialogVisible" title="赛事报名" width="500px">
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="100px">
        <el-form-item label="运动项目" prop="sportItemId">
          <el-select v-model="registerForm.sportItemId" placeholder="请选择">
            <el-option
              v-for="item in availableItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="item.currentParticipants >= item.maxParticipants"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="registerForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="submitRegister">确定报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { get, post } from '@/utils/request'
import { getEventDetail } from '@/api/event'
import { pageSportItems } from '@/api/sportitem'
import type { Event, EventStatus } from '@/types/event'
import type { SportItem } from '@/types/sportitem'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const eventData = ref<Event>({} as Event)
const sportItems = ref<SportItem[]>([])
const schedules = ref<any[]>([])

const registerDialogVisible = ref(false)
const registerLoading = ref(false)
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  sportItemId: null as number | null,
  remark: ''
})

const registerRules: FormRules = {
  sportItemId: [{ required: true, message: '请选择运动项目', trigger: 'change' }]
}

const canRegister = computed(() => {
  return userStore.userInfo?.role === 'STUDENT' &&
         eventData.value.status === 2 &&
         new Date() <= new Date(eventData.value.regEndDate ?? '')
})

const availableItems = computed(() => {
  return sportItems.value.filter(item => item.currentParticipants < item.maxParticipants)
})

onMounted(() => {
  loadEventDetail()
})

async function loadEventDetail() {
  const eventId = Number(route.params.id)
  loading.value = true
  try {
    const res = await getEventDetail(eventId)
    if (res.code === 200) {
      eventData.value = res.data
    }

    const itemsRes = await pageSportItems({ eventId, size: 100 })
    if (itemsRes.code === 200) {
      sportItems.value = itemsRes.data.records
    }

    const schedulesRes = await get('/schedules', { eventId, size: 100 })
    if (schedulesRes.code === 200) {
      schedules.value = schedulesRes.data.records
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/events')
}

function getStatusType(status: EventStatus): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<EventStatus, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'warning'
  }
  return map[status] || 'info'
}

function itemTypeName(type: string) {
  const map: Record<string, string> = {
    TRACK: '径赛',
    FIELD: '田赛',
    BALL: '球类',
    SWIMMING: '游泳',
    WATER: '水上',
    OTHER: '其他'
  }
  return map[type] || type
}

function genderLimitName(limit: number) {
  const map: Record<number, string> = { 0: '不限', 1: '仅男', 2: '仅女' }
  return map[limit] ?? '不限'
}

function scheduleName(item: any) {
  return item.scheduleName || (item.roundNo ? `第${item.roundNo}轮` : '')
}

function getScheduleStatusType(status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = { 1: 'info', 2: 'primary', 3: 'success' }
  return map[status] || 'info'
}

function scheduleStatusName(status: number) {
  const map: Record<number, string> = { 1: '未开始', 2: '进行中', 3: '已结束' }
  return map[status] ?? '未知'
}

function handleRegister() {
  registerForm.sportItemId = null
  registerForm.remark = ''
  registerDialogVisible.value = true
}

function handleRegisterItem(item: SportItem) {
  registerForm.sportItemId = item.id
  registerForm.remark = ''
  registerDialogVisible.value = true
}

async function submitRegister() {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    registerLoading.value = true
    try {
      await post('/registrations', {
        eventId: eventData.value.id,
        sportItemId: registerForm.sportItemId,
        remark: registerForm.remark
      })
      ElMessage.success('报名成功，请等待审核')
      registerDialogVisible.value = false
      loadEventDetail()
    } finally {
      registerLoading.value = false
    }
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
