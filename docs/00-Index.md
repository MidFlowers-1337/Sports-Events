# 校园体育赛事管理系统 - 文档索引

> 基于 SpringBoot + Vue3 的校园体育赛事管理系统设计与实现

## 📚 文档目录

### 1. 需求文档 (01-Requirements)
| 文档 | 说明 | 状态 |
|------|------|------|
| [PRD.md](01-Requirements/PRD.md) | 产品需求文档：背景、目标、MVP范围、角色权限、里程碑 | ✅ 完成 |
| [BusinessRules.md](01-Requirements/BusinessRules.md) | 业务规则：角色权限、报名/赛程/成绩规则、状态机 | ✅ 完成 |

### 2. 架构文档 (02-Architecture)
| 文档 | 说明 | 状态 |
|------|------|------|
| [Architecture.md](02-Architecture/Architecture.md) | 分层架构、模块边界、权限设计、关键流程 | ✅ 完成 |

### 3. API文档 (03-API)
| 文档 | 说明 | 状态 |
|------|------|------|
| [api.md](03-API/api.md) | 接口清单、请求响应示例、权限矩阵、错误码 | ✅ 完成 |

### 4. 数据库文档 (04-DB)
| 文档 | 说明 | 状态 |
|------|------|------|
| [schema.md](04-DB/schema.md) | 数据库设计说明、ER图、索引策略 | ✅ 完成 |
| [schema.sql](04-DB/schema.sql) | 建表SQL脚本 | ✅ 完成 |

### 5. 前端文档 (05-Frontend)
| 文档 | 说明 | 状态 |
|------|------|------|
| [PagesAndFlows.md](05-Frontend/PagesAndFlows.md) | 页面清单、路由菜单、交互流程、异常提示 | ✅ 完成 |
| [RoleWorkbench.md](05-Frontend/RoleWorkbench.md) | 三角色工作台、菜单配置与权限矩阵 | ✅ 完成 |

### 6. 测试文档 (06-Test)
| 文档 | 说明 | 状态 |
|------|------|------|
| [TestPlan.md](06-Test/TestPlan.md) | 测试计划、测试用例、验收标准 | ✅ 完成 |

### 7. 计划文档 (07-Plan)
| 文档 | 说明 | 状态 |
|------|------|------|
| [Backlog.md](07-Plan/Backlog.md) | 需求池、用户故事、优先级 | ✅ 完成 |
| [Sprints.md](07-Plan/Sprints.md) | Sprint计划、任务分解、里程碑 | ✅ 完成 |

---

## 🎯 项目概述

**项目名称**：校园体育赛事管理系统

**技术栈**：
- 后端：SpringBoot + MyBatis-Plus + Spring Security + JWT
- 前端：Vue3 + Element Plus + Vue Router + Axios + ECharts
- 数据库：MySQL 8.0

**核心功能**：
- 报名管理：赛事报名、报名审核/状态管理
- 赛程编排：项目/分组/赛程安排
- 成绩统计：成绩录入、排名、积分
- 赛事资讯发布：公告/新闻
- 历史数据归档：往届赛事/成绩查询

**角色**：
- ADMIN（体育部管理员）
- TEACHER（老师/裁判/教练）
- STUDENT（学生）

---

## 📅 里程碑

| Sprint | 周期 | 目标 |
|--------|------|------|
| Sprint 1 | 第1-2周 | 需求分析与架构设计 |
| Sprint 2 | 第3-4周 | 数据库与API设计 |
| Sprint 3 | 第5-7周 | 后端核心开发 |
| Sprint 4 | 第8-10周 | 前端开发与集成测试 |

---

*文档版本：v1.0.0*
*最后更新：2024-01*