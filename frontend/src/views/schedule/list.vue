<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="赛事">
          <el-select v-model="queryParams.eventId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="比赛日期">
          <el-date-picker v-model="queryParams.matchDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮（仅管理员可编排赛程） -->
      <div class="mb-10" v-if="canManage">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增赛程
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="eventName" label="赛事名称" min-width="150" />
        <el-table-column prop="sportItemName" label="运动项目" width="120" />
        <el-table-column prop="scheduleName" label="赛程名称" width="100" />
        <el-table-column prop="roundNo" label="轮次" width="80" />
        <el-table-column prop="groupNo" label="分组" width="80" />
        <el-table-column prop="matchDate" label="比赛日期" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="endTime" label="结束时间" width="100" />
        <el-table-column prop="location" label="场地" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName || getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage" label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 3" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 2" type="warning" link @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status !== 2" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="mt-20"
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="所属赛事" prop="eventId">
          <el-select v-model="formData.eventId" placeholder="请选择" filterable @change="onEventChange">
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="运动项目" prop="sportItemId">
          <el-select v-model="formData.sportItemId" placeholder="请选择" filterable>
            <el-option v-for="item in sportItemOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="赛程名称" prop="scheduleName">
          <el-input v-model="formData.scheduleName" placeholder="如：预赛、决赛" />
        </el-form-item>
        <el-form-item label="轮次" prop="roundNo">
          <el-input-number v-model="formData.roundNo" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="分组" prop="groupNo">
          <el-input v-model="formData.groupNo" placeholder="如：A组、B组" />
        </el-form-item>
        <el-form-item label="比赛日期" prop="matchDate">
          <el-date-picker v-model="formData.matchDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="formData.startTime" placeholder="选择时间" format="HH:mm" value-format="HH:mm" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="formData.endTime" placeholder="选择时间" format="HH:mm" value-format="HH:mm" />
        </el-form-item>
        <el-form-item label="场地" prop="location">
          <el-input v-model="formData.location" placeholder="请输入比赛场地" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { get, post, put, del } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canManage = computed(() => userStore.isAdmin)

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const eventOptions = ref<any[]>([])
const sportItemOptions = ref<any[]>([])

const queryParams = reactive({
  current: 1,
  size: 10,
  eventId: null as number | null,
  matchDate: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formData = reactive({
  eventId: null as number | null,
  sportItemId: null as number | null,
  scheduleName: '',
  roundNo: 1,
  groupNo: '',
  matchDate: '',
  startTime: '',
  endTime: '',
  location: '',
  remark: ''
})

const rules: FormRules = {
  eventId: [{ required: true, message: '请选择所属赛事', trigger: 'change' }],
  sportItemId: [{ required: true, message: '请选择运动项目', trigger: 'change' }],
  matchDate: [{ required: true, message: '请选择比赛日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

onMounted(() => {
  loadEventOptions()
  loadData()
})

async function loadEventOptions() {
  const res = await get('/events', { size: 100 })
  if (res.code === 200) {
    eventOptions.value = res.data.records || []
  }
}

async function onEventChange(eventId: number) {
  formData.sportItemId = null
  if (eventId) {
    const res = await get('/sport-items', { eventId, size: 100 })
    if (res.code === 200) {
      sportItemOptions.value = res.data.records || []
    }
  } else {
    sportItemOptions.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await get('/schedules', queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryParams.eventId = null
  queryParams.matchDate = ''
  queryParams.current = 1
  loadData()
}

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

function getStatusType(status: number): TagType {
  const map: Record<number, TagType> = {
    1: 'info',      // 未开始
    2: 'primary',   // 进行中
    3: 'success'    // 已结束
  }
  return map[status] || 'info'
}

function getStatusName(status: number) {
  const map: Record<number, string> = {
    1: '未开始',
    2: '进行中',
    3: '已结束',
    4: '已取消'
  }
  return map[status] || '未知'
}

function handleAdd() {
  editingId.value = null
  dialogTitle.value = '新增赛程'
  Object.assign(formData, {
    eventId: null, sportItemId: null, scheduleName: '', roundNo: 1,
    groupNo: '', matchDate: '', startTime: '', endTime: '', location: '', remark: ''
  })
  sportItemOptions.value = []
  dialogVisible.value = true
}

function handleEdit(row: any) {
  editingId.value = row.id
  dialogTitle.value = '编辑赛程'
  Object.assign(formData, row)
  onEventChange(row.eventId)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (editingId.value) {
        await put(`/schedules/${editingId.value}`, formData)
        ElMessage.success('更新成功')
      } else {
        await post('/schedules', formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleStart(row: any) {
  await ElMessageBox.confirm('确定要开始该场比赛吗？', '提示', { type: 'warning' })
  await post(`/schedules/${row.id}/start`)
  ElMessage.success('比赛已开始')
  loadData()
}

async function handleComplete(row: any) {
  await ElMessageBox.confirm('确定要完成该场比赛吗？', '提示', { type: 'warning' })
  await post(`/schedules/${row.id}/complete`)
  ElMessage.success('比赛已完成')
  loadData()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定要删除该赛程吗？', '提示', { type: 'warning' })
  await del(`/schedules/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}
</script>