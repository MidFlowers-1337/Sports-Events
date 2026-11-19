# 前端页面与交互流程文档

> 校园体育赛事管理系统 - 页面设计与交互规范

**文档版本**：v1.0.0  
**创建日期**：2024-01  
**文档状态**：✅ 已评审  
**负责人**：架构师 Winston

---

## 1. 页面清单

### 1.1 公共页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | /login | 用户登录 |
| 404页 | /404 | 页面不存在 |

### 1.2 管理端页面（ADMIN）

| 模块 | 页面 | 路由 | 说明 |
|------|------|------|------|
| 首页 | 统计概览 | /dashboard | 数据统计面板 |
| 赛事管理 | 赛事列表 | /event | 赛事CRUD |
| | 赛事详情 | /event/:id | 查看赛事详情 |
| 项目管理 | 项目列表 | /sport-item | 比赛项目CRUD |
| 报名管理 | 报名列表 | /registration | 报名审核 |
| 赛程管理 | 赛程列表 | /schedule | 赛程编排 |
| 成绩管理 | 成绩列表 | /result | 成绩录入/导入 |
| | 成绩排名 | /result/ranking | 查看排名 |
| 公告管理 | 公告列表 | /news | 公告CRUD |
| 系统管理 | 用户管理 | /system/user | 用户CRUD |
| | 字典管理 | /system/dict | 字典维护 |
| 个人中心 | 个人信息 | /profile | 修改个人信息 |
| | 修改密码 | /password | 修改密码 |

### 1.3 教师端页面（TEACHER）

| 模块 | 页面 | 路由 | 说明 |
|------|------|------|------|
| 首页 | 统计概览 | /dashboard | 数据统计面板 |
| 赛事查看 | 赛事列表 | /event | 查看赛事（只读） |
| 报名管理 | 报名列表 | /registration | 报名审核 |
| 赛程查看 | 赛程列表 | /schedule | 查看赛程 |
| 成绩管理 | 成绩列表 | /result | 成绩录入/导入 |
| | 成绩排名 | /result/ranking | 查看排名 |
| 个人中心 | 个人信息 | /profile | 修改个人信息 |

### 1.4 学生端页面（STUDENT）

| 模块 | 页面 | 路由 | 说明 |
|------|------|------|------|
| 首页 | 赛事大厅 | /home | 赛事列表+公告 |
| 赛事浏览 | 赛事详情 | /event/:id | 查看赛事+报名 |
| 我的报名 | 报名记录 | /my/registration | 我的报名列表 |
| 我的成绩 | 成绩记录 | /my/result | 我的成绩列表 |
| 公告查看 | 公告列表 | /news | 查看公告 |
| | 公告详情 | /news/:id | 公告详情 |
| 个人中心 | 个人信息 | /profile | 修改个人信息 |

---

## 2. 路由配置

### 2.1 路由结构

```javascript
const routes = [
  // 公共路由
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/404', component: NotFound },
  
  // 主布局
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      // 首页
      { path: 'dashboard', component: Dashboard, meta: { title: '首页' } },
      { path: 'home', component: Home, meta: { title: '赛事大厅', roles: ['STUDENT'] } },
      
      // 赛事管理
      { path: 'event', component: EventList, meta: { title: '赛事管理' } },
      { path: 'event/:id', component: EventDetail, meta: { title: '赛事详情' } },
      
      // 项目管理
      { path: 'sport-item', component: SportItemList, meta: { title: '项目管理', roles: ['ADMIN'] } },
      
      // 报名管理
      { path: 'registration', component: RegistrationList, meta: { title: '报名管理', roles: ['ADMIN', 'TEACHER'] } },
      { path: 'my/registration', component: MyRegistration, meta: { title: '我的报名', roles: ['STUDENT'] } },
      
      // 赛程管理
      { path: 'schedule', component: ScheduleList, meta: { title: '赛程管理' } },
      
      // 成绩管理
      { path: 'result', component: ResultList, meta: { title: '成绩管理' } },
      { path: 'result/ranking', component: ResultRanking, meta: { title: '成绩排名' } },
      { path: 'my/result', component: MyResult, meta: { title: '我的成绩', roles: ['STUDENT'] } },
      
      // 公告管理
      { path: 'news', component: NewsList, meta: { title: '公告管理' } },
      { path: 'news/:id', component: NewsDetail, meta: { title: '公告详情' } },
      
      // 系统管理
      { path: 'system/user', component: UserList, meta: { title: '用户管理', roles: ['ADMIN'] } },
      { path: 'system/dict', component: DictList, meta: { title: '字典管理', roles: ['ADMIN'] } },
      
      // 个人中心
      { path: 'profile', component: Profile, meta: { title: '个人信息' } },
      { path: 'password', component: Password, meta: { title: '修改密码' } },
    ]
  },
  
  // 404
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]
```

### 2.2 菜单配置

> 说明：菜单已改为按角色集中配置，实际实现以 [RoleWorkbench.md](RoleWorkbench.md) 为准，下表保留最初的设计草案。

#### ADMIN 菜单
```javascript
const adminMenus = [
  { path: '/dashboard', title: '首页', icon: 'Dashboard' },
  { 
    path: '/event-manage', 
    title: '赛事管理', 
    icon: 'Trophy',
    children: [
      { path: '/event', title: '赛事列表' },
      { path: '/sport-item', title: '项目管理' },
      { path: '/schedule', title: '赛程管理' },
    ]
  },
  { path: '/registration', title: '报名管理', icon: 'Document' },
  { 
    path: '/result-manage', 
    title: '成绩管理', 
    icon: 'Medal',
    children: [
      { path: '/result', title: '成绩录入' },
      { path: '/result/ranking', title: '成绩排名' },
    ]
  },
  { path: '/news', title: '公告管理', icon: 'Bell' },
  { 
    path: '/system', 
    title: '系统管理', 
    icon: 'Setting',
    children: [
      { path: '/system/user', title: '用户管理' },
      { path: '/system/dict', title: '字典管理' },
    ]
  },
]
```

#### TEACHER 菜单
```javascript
const teacherMenus = [
  { path: '/dashboard', title: '首页', icon: 'Dashboard' },
  { path: '/event', title: '赛事查看', icon: 'Trophy' },
  { path: '/registration', title: '报名审核', icon: 'Document' },
  { path: '/schedule', title: '赛程查看', icon: 'Calendar' },
  { 
    path: '/result-manage', 
    title: '成绩管理', 
    icon: 'Medal',
    children: [
      { path: '/result', title: '成绩录入' },
      { path: '/result/ranking', title: '成绩排名' },
    ]
  },
]
```

#### STUDENT 菜单
```javascript
const studentMenus = [
  { path: '/home', title: '赛事大厅', icon: 'Home' },
  { path: '/my/registration', title: '我的报名', icon: 'Document' },
  { path: '/my/result', title: '我的成绩', icon: 'Medal' },
  { path: '/news', title: '公告通知', icon: 'Bell' },
]
```

---

## 3. 页面布局

### 3.1 整体布局

```
+------------------------------------------------------------------+
|                          Header                                   |
|  [Logo] [系统名称]                    [用户头像] [用户名] [退出]    |
+------------------------------------------------------------------+
|        |                                                          |
|        |                                                          |
|  Side  |                      Main Content                        |
|  bar   |                                                          |
|        |                                                          |
|  菜单   |                                                          |
|        |                                                          |
|        |                                                          |
+--------+----------------------------------------------------------+
```

### 3.2 列表页布局

```
+------------------------------------------------------------------+
|  [面包屑导航]                                                      |
+------------------------------------------------------------------+
|  搜索区域                                                          |
|  [输入框] [下拉框] [日期选择] [查询按钮] [重置按钮]                  |
+------------------------------------------------------------------+
|  操作按钮区                                                        |
|  [新增] [批量删除] [导入] [导出]                                    |
+------------------------------------------------------------------+
|  数据表格                                                          |
|  +--------------------------------------------------------------+ |
|  | 序号 | 字段1 | 字段2 | 字段3 | 状态 | 操作                    | |
|  +--------------------------------------------------------------+ |
|  |  1   | xxx   | xxx   | xxx   | 正常 | [详情][编辑][删除]       | |
|  |  2   | xxx   | xxx   | xxx   | 禁用 | [详情][编辑][删除]       | |
|  +--------------------------------------------------------------+ |
+------------------------------------------------------------------+
|  分页                                                              |
|  [共100条] [10条/页 v] [< 1 2 3 4 5 >] [跳至 __ 页]                |
+------------------------------------------------------------------+
```

### 3.3 表单弹窗布局

```
+------------------------------------------+
|  新增/编辑 XXX                      [X]  |
+------------------------------------------+
|                                          |
|  字段1：  [________________]             |
|                                          |
|  字段2：  [________________]             |
|                                          |
|  字段3：  [下拉选择 v      ]             |
|                                          |
|  字段4：  [日期选择        ]             |
|                                          |
|  描述：   [                 ]            |
|           [                 ]            |
|           [_________________]            |
|                                          |
+------------------------------------------+
|              [取消]  [确定]              |
+------------------------------------------+
```

---

## 4. 交互流程

### 4.1 登录流程

```
+-------------+     +-------------+     +-------------+
|   登录页    | --> |  验证账号   | --> |   首页      |
+-------------+     +-------------+     +-------------+
      |                   |
      |                   | 失败
      |                   v
      |             +-------------+
      +------------ |  错误提示   |
                    +-------------+
```

**交互说明**：
1. 用户输入用户名、密码
2. 点击登录按钮
3. 前端校验：用户名、密码不能为空
4. 调用登录接口
5. 成功：存储Token，跳转首页
6. 失败：显示错误提示

### 4.2 学生报名流程

```
+-------------+     +-------------+     +-------------+     +-------------+
|  赛事大厅   | --> |  赛事详情   | --> |  选择项目   | --> |  确认报名   |
+-------------+     +-------------+     +-------------+     +-------------+
                                                                  |
                                                                  v
                                                            +-------------+
                                                            |  报名成功   |
                                                            +-------------+
```

**交互说明**：
1. 学生浏览赛事列表
2. 点击赛事卡片进入详情
3. 查看赛事信息和项目列表
4. 选择要报名的项目
5. 点击报名按钮
6. 弹出确认对话框
7. 确认后提交报名
8. 显示报名成功提示

### 4.3 成绩录入流程

```
+-------------+     +-------------+     +-------------+     +-------------+
|  选择赛事   | --> |  选择项目   | --> |  参赛名单   | --> |  录入成绩   |
+-------------+     +-------------+     +-------------+     +-------------+
                                                                  |
                                              +-------------------+
                                              |
                                              v
                                        +-------------+
                                        |  保存成功   |
                                        +-------------+
```

**交互说明**：
1. 选择赛事
2. 选择比赛项目
3. 显示该项目的参赛人员名单
4. 逐个录入成绩
5. 点击保存
6. 系统自动计算排名
7. 显示保存成功

### 4.4 Excel导入成绩流程

```
+-------------+     +-------------+     +-------------+     +-------------+
|  下载模板   | --> |  填写数据   | --> |  上传文件   | --> |  导入结果   |
+-------------+     +-------------+     +-------------+     +-------------+
```

**交互说明**：
1. 点击"下载模板"获取Excel模板
2. 按模板格式填写成绩数据
3. 点击"导入"按钮
4. 选择Excel文件上传
5. 显示导入进度
6. 显示导入结果（成功数/失败数/错误明细）

---

## 5. 组件规范

### 5.1 表格组件

```vue
<el-table :data="tableData" v-loading="loading" border stripe>
  <el-table-column type="selection" width="50" />
  <el-table-column type="index" label="序号" width="60" />
  <el-table-column prop="name" label="名称" min-width="120" />
  <el-table-column prop="status" label="状态" width="100">
    <template #default="{ row }">
      <el-tag :type="row.status === 1 ? 'success' : 'danger'">
        {{ row.statusName }}
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column label="操作" width="200" fixed="right">
    <template #default="{ row }">
      <el-button type="primary" link @click="handleView(row)">详情</el-button>
      <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
      <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>
```

### 5.2 搜索表单组件

```vue
<el-form :model="queryParams" inline>
  <el-form-item label="名称">
    <el-input v-model="queryParams.name" placeholder="请输入名称" clearable />
  </el-form-item>
  <el-form-item label="状态">
    <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
      <el-option label="正常" :value="1" />
      <el-option label="禁用" :value="0" />
    </el-select>
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="handleQuery">查询</el-button>
    <el-button @click="handleReset">重置</el-button>
  </el-form-item>
</el-form>
```

### 5.3 分页组件

```vue
<el-pagination
  v-model:current-page="queryParams.page"
  v-model:page-size="queryParams.size"
  :page-sizes="[10, 20, 50, 100]"
  :total="total"
  layout="total, sizes, prev, pager, next, jumper"
  @size-change="handleQuery"
  @current-change="handleQuery"
/>
```

---

## 6. 异常提示规范

### 6.1 操作反馈

| 场景 | 提示方式 | 示例 |
|------|----------|------|
| 操作成功 | Message success | "新增成功" |
| 操作失败 | Message error | "新增失败：xxx" |
| 确认操作 | MessageBox confirm | "确定要删除吗？" |
| 表单校验 | 表单项下方红字 | "请输入名称" |
| 加载中 | Loading | 表格/按钮loading |

### 6.2 错误提示

| 错误码 | 提示信息 |
|--------|----------|
| 401 | "登录已过期，请重新登录" |
| 403 | "您没有权限执行此操作" |
| 404 | "请求的资源不存在" |
| 500 | "系统繁忙，请稍后重试" |
| 1001 | "用户名或密码错误" |
| 3001 | "您已报名该项目，请勿重复报名" |
| 3002 | "报名已截止" |

---

## 7. 响应式设计

### 7.1 断点定义

| 断点 | 宽度 | 说明 |
|------|------|------|
| xs | < 768px | 手机 |
| sm | >= 768px | 平板 |
| md | >= 992px | 小屏电脑 |
| lg | >= 1200px | 大屏电脑 |
| xl | >= 1920px | 超大屏 |

### 7.2 响应式规则

| 元素 | 大屏 | 平板 | 手机 |
|------|------|------|------|
| 侧边栏 | 展开 | 收起 | 隐藏 |
| 表格 | 完整显示 | 隐藏部分列 | 卡片模式 |
| 表单 | 多列 | 两列 | 单列 |
| 按钮 | 图标+文字 | 图标+文字 | 仅图标 |

---

## 8. 附录

### 8.1 修订历史

| 版本 | 日期 | 修订人 | 修订内容 |
|------|------|--------|----------|
| v1.0.0 | 2024-01 | Winston (Architect) | 初始版本 |

---

*文档结束*