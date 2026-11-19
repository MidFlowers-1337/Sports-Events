<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="赛事">
          <el-select v-model="queryParams.eventId" placeholder="请选择" clearable filterable @change="handleQueryEventChange">
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="运动项目">
          <el-select v-model="queryParams.sportItemId" placeholder="请选择" clearable filterable>
            <el-option v-for="item in queryItemOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="mb-10">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>录入成绩
        </el-button>
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>导出成绩
        </el-button>
        <el-button type="warning" @click="handleImport">
          <el-icon><Upload /></el-icon>导入成绩
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="eventName" label="赛事名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sportItemName" label="运动项目" width="130" />
        <el-table-column prop="realName" label="参赛者" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="成绩" width="120">
          <template #default="{ row }">{{ displayScore(row) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="ranking" label="名次" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.ranking === 1" type="warning">🥇 第1名</el-tag>
            <el-tag v-else-if="row.ranking === 2" type="info">🥈 第2名</el-tag>
            <el-tag v-else-if="row.ranking === 3" type="danger">🥉 第3名</el-tag>
            <span v-else-if="row.ranking">第{{ row.ranking }}名</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" />
        <el-table-column prop="recorderName" label="记录人" width="100" />
        <el-table-column prop="recordTime" label="记录时间" width="170">
          <template #default="{ row }">{{ row.recordTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="['ADMIN']" type="danger" link @click="handleDelete(row)">删除</el-button>
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
        <template v-if="!editingId">
          <el-form-item label="赛事" prop="eventId">
            <el-select v-model="formData.eventId" placeholder="请选择" filterable @change="onFormEventChange">
              <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="运动项目" prop="sportItemId">
            <el-select v-model="formData.sportItemId" placeholder="请选择" filterable @change="onFormItemChange">
              <el-option v-for="item in formItemOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="参赛者" prop="registrationId">
            <el-select v-model="formData.registrationId" placeholder="仅显示已通过审核的报名" filterable>
              <el-option
                v-for="item in participantOptions"
                :key="item.id"
                :label="`${item.realName}（${item.studentNo || item.username}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="成绩值" prop="score">
          <el-input-number v-model="formData.score" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="成绩文本" prop="scoreText">
          <el-input v-model="formData.scoreText" placeholder="如：11.25秒 / 5.6米" />
        </el-form-item>
        <el-form-item label="名次">
          <el-input-number v-model="formData.ranking" :min="1" :max="999" placeholder="留空自动计算" />
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="formData.points" :min="0" :max="100" placeholder="留空按名次计算" />
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

    <!-- 导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="导入成绩" width="520px">
      <el-form label-width="100px">
        <el-form-item label="赛事">
          <el-select v-model="importEventId" placeholder="请选择" filterable>
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".xlsx,.xls"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择Excel文件</el-button>
            <template #tip>
              <div class="el-upload__tip">按模板填写：学号、运动项目、成绩必填</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" link @click="downloadTemplate">下载导入模板</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">确定导入</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果 -->
    <el-dialog v-model="importResultVisible" title="导入结果" width="600px">
      <p>成功导入 {{ importResult.success }} 条，失败 {{ importResult.failed.length }} 条。</p>
      <el-table v-if="importResult.failed.length" :data="importResult.failed" border size="small" max-height="320">
        <el-table-column prop="row" label="行号" width="80" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="reason" label="失败原因" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules, UploadInstance } from 'element-plus'
import { get, post, put, del, download, upload } from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const eventOptions = ref<any[]>([])
const queryItemOptions = ref<any[]>([])

const queryParams = reactive({
  current: 1,
  size: 10,
  eventId: null as number | null,
  sportItemId: null as number | null
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formItemOptions = ref<any[]>([])
const participantOptions = ref<any[]>([])

const formData = reactive({
  eventId: null as number | null,
  sportItemId: null as number | null,
  registrationId: null as number | null,
  score: undefined as number | undefined,
  scoreText: '',
  ranking: undefined as number | undefined,
  points: undefined as number | undefined,
  remark: ''
})

const rules: FormRules = {
  eventId: [{ required: true, message: '请选择赛事', trigger: 'change' }],
  sportItemId: [{ required: true, message: '请选择运动项目', trigger: 'change' }],
  registrationId: [{ required: true, message: '请选择参赛者', trigger: 'change' }]
}

const importDialogVisible = ref(false)
const importLoading = ref(false)
const importEventId = ref<number | null>(null)
const uploadRef = ref<UploadInstance>()
const importFile = ref<File | null>(null)

const importResultVisible = ref(false)
const importResult = reactive<{ success: number; failed: any[] }>({ success: 0, failed: [] })

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

async function handleQueryEventChange(eventId: number | null) {
  queryParams.sportItemId = null
  queryItemOptions.value = []
  if (!eventId) return
  const res = await get('/sport-items', { eventId, size: 100 })
  if (res.code === 200) {
    queryItemOptions.value = res.data.records || []
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await get('/results', {
      current: queryParams.current,
      size: queryParams.size,
      eventId: queryParams.eventId || undefined,
      sportItemId: queryParams.sportItemId || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.current = 1
  loadData()
}

function resetQuery() {
  queryParams.eventId = null
  queryParams.sportItemId = null
  queryParams.current = 1
  queryItemOptions.value = []
  loadData()
}

function displayScore(row: any) {
  if (row.scoreText) return row.scoreText
  return row.score ?? '-'
}

function handleAdd() {
  editingId.value = null
  dialogTitle.value = '录入成绩'
  Object.assign(formData, {
    eventId: null, sportItemId: null, registrationId: null,
    score: undefined, scoreText: '', ranking: undefined, points: undefined, remark: ''
  })
  formItemOptions.value = []
  participantOptions.value = []
  dialogVisible.value = true
}

function handleEdit(row: any) {
  editingId.value = row.id
  dialogTitle.value = '编辑成绩'
  Object.assign(formData, {
    eventId: row.eventId,
    sportItemId: row.sportItemId,
    registrationId: row.registrationId,
    score: row.score ?? undefined,
    scoreText: row.scoreText || '',
    ranking: row.ranking ?? undefined,
    points: row.points ?? undefined,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function onFormEventChange(eventId: number | null) {
  formData.sportItemId = null
  formData.registrationId = null
  participantOptions.value = []
  if (!eventId) {
    formItemOptions.value = []
    return
  }
  const res = await get('/sport-items', { eventId, size: 100 })
  if (res.code === 200) {
    formItemOptions.value = res.data.records || []
  }
}

async function onFormItemChange(sportItemId: number | null) {
  formData.registrationId = null
  participantOptions.value = []
  if (!sportItemId) return
  const res = await get('/registrations/participants', {
    eventId: formData.eventId,
    sportItemId
  })
  if (res.code === 200) {
    participantOptions.value = res.data || []
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (editingId.value) {
        await put(`/results/${editingId.value}`, {
          score: formData.score,
          scoreText: formData.scoreText || null,
          ranking: formData.ranking,
          points: formData.points,
          remark: formData.remark || null
        })
        ElMessage.success('更新成功')
      } else {
        await post('/results', {
          registrationId: formData.registrationId,
          score: formData.score,
          scoreText: formData.scoreText || null,
          ranking: formData.ranking,
          points: formData.points,
          remark: formData.remark || null
        })
        ElMessage.success('录入成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定要删除该成绩记录吗？删除后会重新计算名次', '提示', { type: 'warning' })
  await del(`/results/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

async function handleExport() {
  if (!queryParams.eventId) {
    ElMessage.warning('请先选择要导出的赛事')
    return
  }
  await download('/results/export', {
    eventId: queryParams.eventId,
    sportItemId: queryParams.sportItemId || undefined
  }, '成绩数据.xlsx')
  ElMessage.success('已开始下载')
}

function handleImport() {
  importEventId.value = null
  importFile.value = null
  importDialogVisible.value = true
}

function handleFileChange(file: any) {
  importFile.value = file.raw
}

async function downloadTemplate() {
  await download('/results/template', undefined, '成绩导入模板.xlsx')
}

async function submitImport() {
  if (!importEventId.value) {
    ElMessage.warning('请选择赛事')
    return
  }
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('eventId', String(importEventId.value))

    const res = await upload('/results/import', formData)
    if (res.code === 200) {
      importResult.success = res.data.success ?? 0
      importResult.failed = res.data.failed || []
      importDialogVisible.value = false
      importResultVisible.value = true
      loadData()
    }
  } finally {
    importLoading.value = false
  }
}
</script>
