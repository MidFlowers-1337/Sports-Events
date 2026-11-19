# 三角色工作台与权限控制

> 校园体育赛事管理系统 - 管理员、教师、学生差异化首页、菜单与权限设计

**文档版本**：v1.0.0  
**适用范围**：前端 `frontend/src`，后端 `backend/src/main/java/com/sports`  
**相关代码**：`router/index.ts`、`config/menus.ts`、`utils/permission.ts`、`views/dashboard/*`、`modules/workbench/*`

---

## 1. 设计目标

系统有管理员（ADMIN）、教师（TEACHER）、学生（STUDENT）三类用户。此前三类用户共用同一个首页和同一套菜单，只靠零散的 `v-if` 控制按钮，存在三个问题：

1. 首页展示的是全站统计，学生登录后看到的报名数、待审核数与本人无关。
2. 菜单按路由表平铺输出，教师能看到自己无权操作的管理入口，点进去后被后端 403 挡回。
3. 权限判断分散在各个页面，新增页面时容易漏掉，前端拦截与后端校验也不一致。

本次改造把角色差异收敛到三处：工作台、菜单配置、权限判定工具；后端补充角色工作台接口，并把统计接口的访问范围限制到管理员和教师。

## 2. 角色与入口

| 角色 | 工作台标题 | 首页内容重点 | 默认入口 |
|------|-----------|-------------|---------|
| 管理员 | 管理工作台 | 全站赛事与报名规模、待审核事项、报名趋势与状态分布 | `/dashboard` |
| 教师 | 教学工作台 | 待审核报名、今日赛程、待录入成绩、成绩记录 | `/dashboard` |
| 学生 | 我的工作台 | 可报名赛事、我的报名进度、我的成绩与奖牌、公告 | `/dashboard` |

三类用户共用 `/dashboard` 路由，由 `views/dashboard/index.vue` 读取 `userStore.userInfo.role` 后渲染对应组件，避免登录后还要二次跳转。

## 3. 菜单结构

菜单集中定义在 `frontend/src/config/menus.ts`，按角色给出树形结构。`layout/index.vue` 只负责渲染，不再从路由表里推导菜单。

### 3.1 管理员菜单

| 一级 | 二级 |
|------|------|
| 管理工作台 | - |
| 赛事管理 | 赛事列表、运动项目、赛程编排 |
| 报名审核 | - |
| 成绩管理 | - |
| 统计分析 | - |
| 公告管理 | - |
| 用户管理 | - |

### 3.2 教师菜单

| 一级 | 二级 |
|------|------|
| 教学工作台 | - |
| 赛事查看 | - |
| 报名审核 | - |
| 赛程查看 | - |
| 成绩录入 | - |
| 统计分析 | - |
| 公告通知 | - |

### 3.3 学生菜单

| 一级 | 二级 |
|------|------|
| 我的工作台 | - |
| 赛事大厅 | - |
| 我的报名 | - |
| 我的成绩 | - |
| 赛程安排 | - |
| 公告通知 | - |

菜单路径与 `router/index.ts` 中注册的路由一一对应。新增页面时两处同时修改，路由的 `meta.roles` 仍然保留，用于直接输入 URL 时的拦截。

## 4. 工作台接口

后端新增 `GET /api/workbench/summary`，角色从 JWT 中读取，前端无法通过参数切换身份。返回结构如下：

| 字段 | 管理员 | 教师 | 学生 | 说明 |
|------|--------|------|------|------|
| `role` / `roleName` | ✓ | ✓ | ✓ | 当前角色 |
| `today` | ✓ | ✓ | ✓ | 服务器日期，用于今日赛程 |
| `cards` | 全站指标 | 审核与赛程指标 | 个人指标 | 首页指标卡数据 |
| `todaySchedules` | ✓ | ✓ | - | 今日赛程，最多 5 条 |
| `recentPendingRegistrations` | ✓ | ✓ | - | 待审核报名，最多 5 条 |
| `availableEvents` | - | - | ✓ | 报名中的赛事，最多 6 条 |
| `myRecentRegistrations` | - | - | ✓ | 本人最近报名，最多 5 条 |
| `myRecentResults` | - | - | ✓ | 本人最近成绩，最多 5 条 |
| `recentNews` | - | - | ✓ | 已发布公告，最多 5 条 |

`cards` 字段含义：

| 角色 | 字段 |
|------|------|
| 管理员 | `totalEvents`、`ongoingEvents`、`totalRegistrations`、`pendingRegistrations`、`totalResults`、`totalUsers`、`todaySchedules` |
| 教师 | `pendingRegistrations`、`todaySchedules`、`pendingResults`、`totalResults` |
| 学生 | `myRegistrations`、`approvedRegistrations`、`pendingRegistrations`、`myResults`、`goldMedals`、`silverMedals`、`bronzeMedals` |

其中 `pendingResults` 为已通过审核但尚未录入成绩的报名数，口径按报名记录与成绩记录的差集计算。

## 5. 权限控制

权限控制分四层，任一层单独失效都不会造成越权。

### 5.1 路由守卫

`router/index.ts` 的 `beforeEach` 在放行前依次检查：是否公开页面、是否已登录、内存中的用户信息是否有效、路由 `meta.roles` 是否包含当前角色。不满足时跳转 `/403`，并带上来源路径。

### 5.2 菜单过滤

`getMenusByRole(role)` 只返回该角色的菜单树。未知角色返回空菜单，页面其余部分仍可访问，但不会看到任何业务入口。

### 5.3 按钮级指令

`v-permission` 指令在挂载时判断角色，不在允许列表内直接从 DOM 移除节点。

```vue
<el-button v-permission="['ADMIN']" type="danger" @click="handleDelete">删除</el-button>
<el-button v-permission="['ADMIN', 'TEACHER']" @click="handleAdd">新增</el-button>
```

表格列无法用指令移除，使用 `v-if`：

```vue
<el-table-column v-if="canManage" label="操作" />
```

### 5.4 后端接口校验

| 接口 | 允许角色 | 实现方式 |
|------|---------|---------|
| `GET /api/workbench/summary` | 所有已登录角色 | 角色从 JWT 读取，返回对应数据 |
| `/api/statistics/**` | ADMIN、TEACHER | 类级 `@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")` |
| `/api/users/**` | ADMIN | 方法级 `@PreAuthorize("hasRole('ADMIN')")` |
| 赛事增删改、发布、取消 | ADMIN | 方法级注解 |
| 报名审核、成绩录入、导入导出 | ADMIN、TEACHER | 方法级注解 |
| `GET /api/results` | 所有已登录角色 | 服务层对 STUDENT 强制按 `userId` 过滤 |
| 公告增删改、上下架 | ADMIN | 方法级注解 |

前端拦截只影响体验，真正的越权防护由后端注解与服务层过滤完成。

## 6. 权限矩阵

| 功能 | 管理员 | 教师 | 学生 |
|------|:------:|:----:|:----:|
| 工作台 | 全站视图 | 教学视图 | 个人视图 |
| 赛事列表 | 增删改查 | 只读 | 只读（仅已发布） |
| 运动项目 | 增删改查 | 只读 | 不可见 |
| 报名审核 | ✓ | ✓ | 不可见 |
| 我的报名 | 不可见 | 不可见 | ✓ |
| 赛程 | 增删改查 | 只读 | 只读 |
| 成绩管理 | 增删改查 | 录入、修改、导入导出 | 不可见 |
| 我的成绩 | 不可见 | 不可见 | ✓ |
| 统计分析 | ✓ | ✓ | 不可见 |
| 公告管理 | 增删改查 | 只读 | 只读（仅已发布） |
| 用户管理 | ✓ | 不可见 | 不可见 |

## 7. 关键文件

| 文件 | 作用 |
|------|------|
| `frontend/src/config/menus.ts` | 三类角色的菜单树与工作台文案 |
| `frontend/src/utils/permission.ts` | `hasRole`、`isAdmin`、`isManager` 等判定函数 |
| `frontend/src/directives/permission.ts` | `v-permission` 按钮级指令 |
| `frontend/src/layout/index.vue` | 分组菜单渲染、头部角色标识 |
| `frontend/src/views/dashboard/index.vue` | 按角色分发工作台组件 |
| `frontend/src/views/dashboard/AdminWorkbench.vue` | 管理员工作台 |
| `frontend/src/views/dashboard/TeacherWorkbench.vue` | 教师工作台 |
| `frontend/src/views/dashboard/StudentWorkbench.vue` | 学生工作台 |
| `frontend/src/views/error/403.vue` | 无权限提示页 |
| `backend/.../modules/workbench/**` | 角色工作台接口与服务实现 |

## 8. 验证方式

1. 使用 admin、teacher、student 三个账号分别登录，确认默认菜单与首页内容不同。
2. 学生账号直接访问 `/users`、`/statistics`、`/registrations`，确认跳转 `/403`。
3. 学生账号调用 `/api/statistics/overview`，确认返回 403。
4. 教师账号进入赛程页，确认看不到新增、编辑、删除按钮。
5. 学生账号调用 `/api/results`，确认只返回本人成绩。
6. 执行 `npm run build:check` 与后端 `mvn clean package -DskipTests`，确认构建通过。
