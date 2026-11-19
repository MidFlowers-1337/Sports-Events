<template>
  <AdminWorkbench v-if="role === 'ADMIN'" />
  <TeacherWorkbench v-else-if="role === 'TEACHER'" />
  <StudentWorkbench v-else-if="role === 'STUDENT'" />
  <el-result
    v-else
    icon="warning"
    title="未识别账号角色"
    sub-title="当前账号没有匹配的工作台，请确认角色配置或联系管理员处理"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import AdminWorkbench from './AdminWorkbench.vue'
import TeacherWorkbench from './TeacherWorkbench.vue'
import StudentWorkbench from './StudentWorkbench.vue'

const userStore = useUserStore()
const role = computed(() => userStore.userInfo?.role || '')
</script>
