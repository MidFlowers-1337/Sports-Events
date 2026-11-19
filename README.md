# 校园体育赛事管理系统

基于 SpringBoot + Vue3 的校园体育赛事管理系统，面向高校体育管理部门、教师和学生，实现赛事报名、赛程编排、成绩统计、资讯发布等功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **持久层**: MyBatis-Plus 3.5.5
- **安全**: Spring Security 6 + JWT
- **数据库**: MySQL 8.0
- **文档**: Knife4j (Swagger)
- **工具**: Lombok, Hutool

### 前端
- **框架**: Vue 3.4 + TypeScript
- **UI组件**: Element Plus 2.4
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **图表**: ECharts 5
- **HTTP**: Axios

## 功能模块

### 角色权限
- **管理员(ADMIN)**: 管理工作台、赛事与项目管理、报名审核、赛程编排、成绩管理、统计分析、公告与用户管理
- **教师(TEACHER)**: 教学工作台、赛事查看、报名审核、赛程查看、成绩录入与导入导出、统计分析
- **学生(STUDENT)**: 我的工作台、赛事浏览与报名、我的报名、我的成绩、赛程与公告查看

三类角色使用各自的工作台和菜单，路由、菜单、按钮与后端接口四层校验。设计说明见 [RoleWorkbench.md](docs/05-Frontend/RoleWorkbench.md)。

### 核心功能
1. **赛事管理**: 赛事CRUD、发布/取消、状态管理
2. **运动项目**: 项目配置、分类管理
3. **报名管理**: 在线报名、审核流程
4. **赛程编排**: 赛程安排、场地分配
5. **成绩管理**: 成绩录入、排名计算、Excel导入导出
6. **统计分析**: ECharts可视化、数据报表
7. **资讯发布**: 公告/新闻管理

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS sports_events DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构
mysql -u root -p sports_events < docs/04-DB/schema.sql

-- 导入测试数据
mysql -u root -p sports_events < backend/src/main/resources/db/data.sql
```

### 后端启动

```bash
cd backend

# 修改数据库配置 (application.yml)
# spring.datasource.url, username, password

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/sports-events-1.0.0.jar

# 或使用Maven直接运行
mvn spring-boot:run
```

后端启动后访问:
- API文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端启动后访问: http://localhost:5173

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher | 123456 |
| 学生 | student | 123456 |

## 项目结构

```
Sports-Events/
├── backend/                    # 后端项目
│   ├── src/main/java/com/sports/
│   │   ├── common/            # 通用模块
│   │   │   ├── config/        # 配置类
│   │   │   ├── entity/        # 基础实体
│   │   │   ├── exception/     # 异常处理
│   │   │   └── result/        # 统一响应
│   │   ├── modules/           # 业务模块
│   │   │   ├── auth/          # 认证模块
│   │   │   ├── event/         # 赛事模块
│   │   │   ├── sportitem/     # 运动项目
│   │   │   ├── registration/  # 报名模块
│   │   │   ├── schedule/      # 赛程模块
│   │   │   ├── result/        # 成绩模块
│   │   │   ├── news/          # 资讯模块
│   │   │   ├── statistics/    # 统计模块
│   │   │   └── system/        # 系统模块
│   │   └── security/          # 安全模块
│   └── src/main/resources/
│       ├── application.yml    # 应用配置
│       └── db/                # 数据库脚本
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── layout/            # 布局组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # 状态管理
│   │   ├── styles/            # 全局样式
│   │   ├── types/             # 类型定义
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件
│   └── vite.config.ts         # Vite配置
└── docs/                       # 项目文档
    ├── 00-Index.md            # 文档索引
    ├── 01-Requirements/       # 需求文档
    ├── 02-Architecture/       # 架构设计
    ├── 03-API/                # API文档
    ├── 04-DB/                 # 数据库设计
    ├── 05-Frontend/           # 前端设计
    └── 07-Plan/               # 项目计划
```

## API接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取用户信息
- `POST /api/auth/logout` - 退出登录

### 赛事接口
- `GET /api/events` - 赛事列表
- `POST /api/events` - 创建赛事
- `GET /api/events/{id}` - 赛事详情
- `PUT /api/events/{id}` - 更新赛事
- `DELETE /api/events/{id}` - 删除赛事
- `POST /api/events/{id}/publish` - 发布赛事

### 报名接口
- `GET /api/registrations` - 报名列表
- `POST /api/registrations` - 提交报名
- `POST /api/registrations/{id}/approve` - 审核通过
- `POST /api/registrations/{id}/reject` - 审核拒绝

### 成绩接口
- `GET /api/results` - 成绩列表
- `POST /api/results` - 录入成绩
- `GET /api/results/export` - 导出成绩
- `POST /api/results/import` - 导入成绩

详细API文档请参考: [docs/03-API/api.md](docs/03-API/api.md)

## 演示流程

### 管理员操作
1. 登录系统 → 创建赛事 → 添加运动项目 → 发布赛事
2. 审核报名 → 编排赛程 → 录入成绩
3. 查看统计报表

### 学生操作
1. 注册/登录 → 浏览赛事 → 选择项目报名
2. 查看报名状态 → 查看赛程安排
3. 查看比赛成绩和排名

### 教师操作
1. 登录系统 → 查看赛程安排
2. 录入/导入比赛成绩
3. 审核学生报名（可选）

## 开发说明

### 代码规范
- 后端遵循阿里巴巴Java开发规范
- 前端遵循Vue3 Composition API风格
- 统一使用UTF-8编码

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试相关

## 文档

- [产品需求文档](docs/01-Requirements/PRD.md)
- [业务规则](docs/01-Requirements/BusinessRules.md)
- [架构设计](docs/02-Architecture/Architecture.md)
- [API接口文档](docs/03-API/api.md)
- [数据库设计](docs/04-DB/schema.md)
- [前端页面设计](docs/05-Frontend/PagesAndFlows.md)
- [项目计划](docs/07-Plan/Sprints.md)

## License

MIT License