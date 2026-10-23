<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="queryParams.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable>
            <el-option label="公告" value="ANNOUNCEMENT" />
            <el-option label="新闻" value="NEWS" />
            <el-option label="通知" value="NOTICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="mb-10" v-if="isAdmin">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>发布资讯
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="statusName" label="状态" width="100" v-if="isAdmin">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleOffline(row)">下架</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择">
            <el-option label="公告" value="ANNOUNCEMENT" />
            <el-option label="新闻" value="NEWS" />
            <el-option label="通知" value="NOTICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联赛事" prop="eventId">
          <el-select v-model="formData.eventId" placeholder="请选择（可选）" clearable filterable>
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="10" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="置顶" prop="isTop">
          <el-switch v-model="formData.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" :title="currentNews.title" width="800px">
      <div class="news-detail">
        <div class="news-meta">
          <span>作者：{{ currentNews.authorName }}</span>
          <span>发布时间：{{ currentNews.publishTime }}</span>
          <span>浏览量：{{ currentNews.viewCount }}</span>
        </div>
        <el-divider />
        <div class="news-content" v-html="currentNews.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { pageNews, getNewsDetail, createNews, updateNews, deleteNews, publishNews, offlineNews } from '@/api/news'
import { pageEvents } from '@/api/event'
import type { Event } from '@/types/event'
import type { News, NewsDTO } from '@/types/news'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const loading = ref(false)
const tableData = ref<News[]>([])
const total = ref(0)
const eventOptions = ref<Event[]>([])

const queryParams = reactive({
  current: 1,
  size: 10,
  title: '',
  type: '',
  status: null as number | null
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const formData = reactive<NewsDTO>({
  title: '',
  type: '',
  eventId: null,
  content: '',
  isTop: 0
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const currentNews = ref<News>({} as News)

onMounted(() => {
  loadEventOptions()
  loadData()
})

async function loadEventOptions() {
  const res = await pageEvents({ size: 100 })
  if (res.code === 200) {
    eventOptions.value = res.data.records
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageNews(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryParams.title = ''
  queryParams.type = ''
  queryParams.status = null
  queryParams.current = 1
  loadData()
}

function getTypeTagType(type: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    ANNOUNCEMENT: 'danger',
    NEWS: 'primary',
    NOTICE: 'warning'
  }
  return map[type] || 'info'
}

function getStatusType(status: number): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<number, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

function handleAdd() {
  editingId.value = null
  dialogTitle.value = '发布资讯'
  Object.assign(formData, {
    title: '', type: '', eventId: null, content: '', isTop: 0
  })
  dialogVisible.value = true
}

function handleEdit(row: News) {
  editingId.value = row.id
  dialogTitle.value = '编辑资讯'
  Object.assign(formData, {
    title: row.title,
    type: row.type,
    eventId: row.eventId ?? null,
    content: row.content,
    isTop: row.isTop
  })
  dialogVisible.value = true
}

async function handleView(row: News) {
  const res = await getNewsDetail(row.id)
  if (res.code === 200) {
    currentNews.value = res.data
    detailDialogVisible.value = true
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (editingId.value) {
        await updateNews(editingId.value, formData)
        ElMessage.success('更新成功')
      } else {
        await createNews(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function handlePublish(row: News) {
  await ElMessageBox.confirm('确定要发布该资讯吗？', '提示', { type: 'warning' })
  await publishNews(row.id)
  ElMessage.success('发布成功')
  loadData()
}

async function handleOffline(row: News) {
  await ElMessageBox.confirm('确定要下架该资讯吗？', '提示', { type: 'warning' })
  await offlineNews(row.id)
  ElMessage.success('下架成功')
  loadData()
}

async function handleDelete(row: News) {
  await ElMessageBox.confirm('确定要删除该资讯吗？', '提示', { type: 'warning' })
  await deleteNews(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.news-detail {
  padding: 20px;
}

.news-meta {
  color: #909399;
  font-size: 14px;
}

.news-meta span {
  margin-right: 20px;
}

.news-content {
  line-height: 1.8;
  font-size: 15px;
}
</style>
