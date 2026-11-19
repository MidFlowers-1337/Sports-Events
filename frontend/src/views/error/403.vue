<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <h2 class="error-title">没有访问权限</h2>
      <p class="error-desc">当前账号的角色无权访问该页面，如需使用请联系管理员调整角色</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回工作台</el-button>
        <el-button @click="goBack">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}

function goBack() {
  router.go(-1)
}
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  color: #fff;
}

.error-code {
  font-size: 150px;
  font-weight: bold;
  margin: 0;
  text-shadow: 4px 4px 0 rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 32px;
  margin: 20px 0;
}

.error-desc {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 30px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
</style>
