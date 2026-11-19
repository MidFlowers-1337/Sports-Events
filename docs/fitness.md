# 体测扩展（课程设计简化版）

保留原赛事业务，新增独立体测模块，不依赖Python或额外算法库。

## 数据库

先按原README初始化数据库，再在 sports_events 数据库执行 docs/04-DB/fitness.sql。
脚本不删除旧表。每名学生每天一条记录，外键关联sys_user；更新时提交完整记录。

## 接口

沿用JWT认证。管理员、教师可录入、修改和聚类，学生只能查询本人。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/fitness | 录入 |
| PUT | /api/fitness/{id} | 修改，不存在时报错 |
| GET | /api/fitness?current=1&size=10&userId=3&testDate=2026-11-18 | 分页查询，筛选可选 |
| GET | /api/fitness/clusters?testDate=2026-11-18&k=3 | 按测试日期聚类，k为1至10 |

录入、修改请求示例（userId换成数据库中有效学生ID，日期不可晚于当天）：

```json
{"userId":3,"testDate":"2026-11-18","heightCm":175,"weightKg":65,"vitalCapacity":4000,"run50Seconds":7.5,"jumpCm":220}
```

## 预处理、算法与分析

排除缺失、非有限、超出录入范围的历史异常值，不对缺失值猜测填充；返回excludedCount。
特征依次为BMI、肺活量、50米用时、立定跳远。先做Z-score标准化，常量列尺度设为1。
使用确定性最远点初始化，最多迭代100次；相同分组时停止，空簇保留旧中心。
不同特征样本数不足k、无有效样本时返回业务错误；单次最多5000条。

recordIds与analysis.labels逐一对应；标签从0开始。
analysis.counts为各簇人数；centers还原至原始单位，便于比较各组特点；
sse为标准化空间内的簇内平方误差，只适合在同一批数据和特征上比较。
iterations为迭代次数。簇编号不表示优劣；50米越低通常越好，肺活量和跳远越高通常越好，
BMI不是越大越好。这里只做探索性分析，不替代国家体测评分，未按性别分层。

## 测试

在backend执行 `mvn test -Dtest=KMeansTest`。
算法测试覆盖分组、中心还原、常量列、非法输入、重复样本和确定性。
接口联调需启动MySQL并执行新增SQL：验证教师录入和修改、重复录入被拒绝、
不存在ID修改报错、学生传入他人userId仍只能看到本人、学生调用聚类被拒绝。
