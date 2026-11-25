# 校园体育赛事与学生体质健康管理系统

贵州大学软件工程课程设计项目。在校园赛事管理的基础上，加入三角色工作台、体测记录和 K-Means 分析。

## Docker 一键启动

Windows 上安装 Docker Desktop 并完成首次设置，使用 Linux 容器模式。解压项目后，**双击项目根目录中的 [启动系统.bat](启动系统.bat)**。脚本会打开 Docker Desktop（如果还没运行），启动 MySQL、Spring Boot 后端和 Vue 前端，等服务就绪后自动打开 [系统首页](http://127.0.0.1:8088/)。

首次启动要联网下载镜像和构建依赖，可能需要几分钟。Java、Node.js、MySQL 都由容器提供，演示电脑只需准备 Docker Desktop。

Docker 已经运行时，也可以在项目根目录执行下面的命令。

```sh
docker compose up -d --build --wait
```

启动完成后访问 [系统首页](http://127.0.0.1:8088/) 或 [接口文档](http://127.0.0.1:8088/doc.html)。直接运行 `docker compose up` 也会启动全部服务，并在当前终端显示日志。

| 角色 | 演示账号 | 演示密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `teacher1` | `123456` |
| 学生 | `student1` | `123456` |

- 停止时双击 [停止系统.bat](停止系统.bat)，或在项目根目录运行 `docker compose down`。MySQL 数据卷会保留，下次启动会继续使用。
- 首次创建空数据卷时，MySQL 按顺序加载 `schema.sql`、`data.sql` 和 `fitness.sql`。这个演示数据库随 Compose 项目管理，只有前端端口映射到本机。
- 8088 端口被占用时，可在项目根目录新建 `.env`，写入一行 `SPORTS_APP_PORT=8089` 后重启。双击脚本会读取实际端口。
- 如果启动失败，运行 `docker compose ps` 查看服务状态，运行 `docker compose logs --tail 100` 查看日志。首次下载失败时，检查 Docker Desktop 的网络或代理设置后重试。

这些账号和数据库默认配置适用于本机演示。启动顺序使用 Compose 的 [服务健康检查](https://docs.docker.com/compose/how-tos/startup-order/)。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.1
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

## 当前功能

- 赛事管理、运动项目配置、报名与审核、赛程安排、比赛成绩、公告和统计。
- 管理员、教师、学生的独立工作台与菜单。后端根据登录角色返回对应的工作台摘要。
- 体测记录表与录入、修改、查询接口。管理员和教师也可以调用聚类接口，学生查询范围为本人。
- K-Means 对 BMI、肺活量、50 米用时和立定跳远进行标准化后聚类，返回簇人数、聚类中心与误差指标。

体测专用页面、国家标准评分与等级、个人报告、历史趋势和聚类图表仍在后续计划中。

## 本地开发（可选）

开发环境使用 JDK 17 或更高版本、Maven 3.8 或更高版本、Node.js 20 或更高版本以及 MySQL 8。以下命令以 Windows PowerShell 为例。

**仅在可清空的本地演示数据库执行初始化。** `schema.sql` 固定使用 `sports_events`，会删除并重建表。`data.sql` 会清空业务表后写入演示数据。

在项目根目录按顺序执行以下命令。

```powershell
mysql --default-character-set=utf8mb4 -u root -p -e "source docs/04-DB/schema.sql"
mysql --default-character-set=utf8mb4 -u root -p sports_events -e "source backend/src/main/resources/db/data.sql"
mysql --default-character-set=utf8mb4 -u root -p sports_events -e "source docs/04-DB/fitness.sql"
```

打开一个终端，从项目根目录打包并启动后端。按需修改 MySQL 地址和账户。

```powershell
cd backend
mvn -B -DskipTests package
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3306/sports_events?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
$env:SPRING_DATASOURCE_USERNAME = 'root'
$env:SPRING_DATASOURCE_PASSWORD = '替换为你的 MySQL 密码'
java -jar target/sports-events-1.0.0.jar
```

另开一个终端，从项目根目录执行以下命令来启动前端。

```powershell
cd frontend
npm ci --no-audit --no-fund --prefer-online
npm run dev
```

开发页面默认在 `http://localhost:3000/`，`/api` 代理到 `http://localhost:8080`。后端接口文档位于 `http://localhost:8080/doc.html`。

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
- `PUT /api/auth/profile` - 修改个人资料

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

工作台与体测接口见 [工作台设计](docs/05-Frontend/RoleWorkbench.md) 和 [体测接口说明](docs/fitness.md)。其他接口见 [docs/03-API/api.md](docs/03-API/api.md)

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
