<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">体育赛事管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="node in menus" :key="isMenuGroup(node) ? node.title : node.path">
          <el-sub-menu v-if="isMenuGroup(node)" :index="node.title">
            <template #title>
              <el-icon><component :is="node.icon" /></el-icon>
              <span>{{ node.title }}</span>
            </template>
            <el-menu-item v-for="child in node.children" :key="child.path" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="node.path">
            <el-icon><component :is="node.icon" /></el-icon>
            <template #title>{{ node.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute?.meta?.title && currentRoute.path !== '/dashboard'">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName }}</span>
              <el-tag class="role-tag" size="small" effect="plain">{{ roleName }}</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px" @closed="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMenusByRole, isMenuGroup } from '@/config/menus'
import { ROLE_LABELS } from '@/utils/permission'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { post } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: any, value: string, callback: Function) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

function resetPwdForm() {
  pwdFormRef.value?.resetFields()
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

async function submitPwd() {
  if (!pwdFormRef.value) return
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await post('/auth/change-password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdVisible.value = false
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 800)
  } finally {
    pwdLoading.value = false
  }
}

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

// 按当前登录角色取侧边栏菜单
const menus = computed(() => getMenusByRole(userStore.userInfo?.role))

// 头部角色标识
const roleName = computed(
  () => userStore.userInfo?.roleName || ROLE_LABELS[userStore.userInfo?.role || ''] || ''
)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      resetPwdForm()
      pwdVisible.value = true
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
      })
      break
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100%;
}

.layout-aside {
  background: var(--sidebar-bg);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
  
  // 侧边栏光效装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 1px;
    background: linear-gradient(180deg, transparent 0%, rgba(255,255,255,0.1) 50%, transparent 100%);
  }

  .logo {
    height: var(--header-height);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    gap: 12px;
    
    .logo-img {
      width: 40px;
      height: 40px;
      flex-shrink: 0;
      filter: drop-shadow(0 2px 8px rgba(124, 58, 237, 0.4));
    }

    .logo-text {
      color: #fff;
      font-size: 17px;
      font-weight: 700;
      white-space: nowrap;
      letter-spacing: 0.5px;
      background: linear-gradient(135deg, #ffffff 0%, #c4b5fd 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .el-menu {
    border-right: none !important;
    padding: 8px 0;
    background: transparent !important;
    
    .el-menu-item {
      color: rgba(255, 255, 255, 0.75) !important;
      height: 46px !important;
      line-height: 46px !important;
      font-weight: 500;
      
      & .el-icon {
        color: rgba(255, 255, 255, 0.7) !important;
        font-size: 18px;
      }
      
      &.is-active {
        background: linear-gradient(135deg, rgba(37, 99, 235, 0.95) 0%, rgba(124, 58, 237, 0.95) 100%) !important;
        color: #fff !important;
        box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
        
        & .el-icon {
          color: #fff !important;
        }
      }
      
      &:hover:not(.is-active) {
        background-color: rgba(255, 255, 255, 0.08) !important;
        color: #fff !important;
        
        & .el-icon {
          color: rgba(255, 255, 255, 0.9) !important;
        }
      }
    }
  }
}

.layout-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: var(--header-height);

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      font-size: 22px;
      cursor: pointer;
      margin-right: 24px;
      color: var(--text-secondary);
      padding: 8px;
      border-radius: var(--radius-md);
      transition: all 0.25s ease;

      &:hover {
        color: var(--primary);
        background: var(--primary-bg);
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 6px 14px 6px 6px;
      border-radius: 999px;
      transition: all 0.25s ease;
      
      &:hover {
        background: var(--bg-hover);
      }

      .username {
        margin: 0 10px;
        font-weight: 500;
        color: var(--text-primary);
      }
    }
  }
}

.layout-main {
  background: linear-gradient(180deg, var(--bg-page) 0%, #E2E8F0 100%);
  padding: 24px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

// 分组菜单标题与子菜单项
.layout-aside :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.75) !important;
  height: 46px !important;
  line-height: 46px !important;
  font-weight: 500;

  .el-icon {
    color: rgba(255, 255, 255, 0.7) !important;
    font-size: 18px;
  }

  &:hover {
    background-color: rgba(255, 255, 255, 0.08) !important;
    color: #fff !important;
  }
}

.layout-aside :deep(.el-sub-menu .el-menu-item) {
  padding-left: 48px !important;
  min-width: auto !important;
  background: transparent !important;
}

.layout-aside :deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
  margin-right: 0;
}

.role-tag {
  margin-right: 6px;
  border-color: rgba(37, 99, 235, 0.35);
  color: var(--primary);
  background: var(--primary-bg);
}
</style>