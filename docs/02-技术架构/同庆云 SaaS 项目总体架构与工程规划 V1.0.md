# 同庆云 SaaS 项目总体架构与工程规划 V1.0

> **文档状态：** 待最终确认  
> **工程名称：** `lingjuli-operation`  
> **适用对象：** 产品、设计、后端、前端、测试、运维及 Codex 开发 Agent  
> **文档目的：** 统一项目定位、技术栈、模块与服务边界、工程目录、设计系统、环境配置和分阶段交付标准，为从零搭建工程提供基线。  
> **重要说明：** 本文包含已确认决策与尚待确认建议；不得把“待确认”内容当作正式冻结的开发要求。

---

## 1. 项目定位与基本原则

### 1.1 项目定位

**同庆云（TongQing Cloud）** 是面向连锁餐饮企业的智能经营 SaaS 平台。由零距离网络科技建设，首个接入商户为同庆楼餐饮集团，后续应支持更多独立餐饮商户接入。

| 项目要素 | 定义 |
| --- | --- |
| 项目根目录 | `lingjuli-operation` |
| 产品名称 | 同庆云（TongQing Cloud） |
| 开发方式 | Codex 辅助的 Vibe Coding，基于明确规则与验收闭环 |
| 架构原则 | SaaS First、Maven 多模块、适度微服务、领域边界清晰 |
| 应用终端 | SaaS 平台 Web、商户 Web、移动 App |
| 首个商户 | 同庆楼餐饮集团；代码不得硬编码首商户专属逻辑 |
| 设计体系 | Lingjuli Design System，轻量现代型 |

### 1.2 产品目标

- 多餐饮商户独立入驻，数据与权限严格隔离。
- 平台提供租户、套餐、订阅与资源配额管理。
- 商户支持集团、区域、品牌、业态、门店等经营维度。
- 集成金蝶、人力管家、企业微信、钉钉、POS 等外部系统。
- 逐步覆盖商品、物料、BOM、成本、经营分析、预测、DTC、巡检与人力业务。
- 所有 Web 业务页面复用统一的 Design Tokens、Arco Vue 和公共业务组件。
- 支持多人协作、可重复部署、可追踪测试和长期升级维护。

### 1.3 架构底线

1. **SaaS First 不等于所有模块都独立部署。** 第一阶段保留五个独立服务，其余领域在 Business 内部采用 Maven 多模块。
2. **多租户隔离与组织数据权限是两套机制。** 任何 API 都必须在服务端同时执行相关授权。
3. **MySQL 为业务事务权威数据源。** Doris 用于经营分析与向量检索，Milvus 作为可选扩展。
4. **服务之间禁止跨库直接修改他人业务表。** 优先使用受控 API 或可靠事件。
5. **优先真实业务可运行、可测试，再考虑进一步拆服务。**
6. **平台端与商户端为两个独立 Web 应用，共享同一设计系统。**
7. **Design System 先于业务页面建设，并通过真实 Vue 页面与视觉回归验收。**

---

## 2. 技术栈基线

> 核心产品与主版本线已经讨论确定。各依赖的**精确补丁版本**应由初始化工程的依赖兼容性验证统一锁定，并提交锁文件，不得使用动态版本。

### 2.1 后端与基础设施

| 层级 | 技术选型 | 备注 |
| --- | --- | --- |
| Java | Java 17 LTS | 已确认 |
| 基础框架 | Spring Boot 3.5.x | 选定主版本线 |
| 微服务 | Spring Cloud 2025.0.x | 与 Boot 版本协同验证 |
| 阿里微服务生态 | Spring Cloud Alibaba 2025.0.x | 根据实际组件兼容性锁版本 |
| API 网关 | Spring Cloud Gateway | 独立部署 |
| 注册与配置 | Nacos 3.2.4 | 已部署，作为服务注册与配置中心 |
| 身份权限 | Sa-Token | 统一认证会话；未来标准 OIDC 接入单独评估 |
| 持久层 | MyBatis-Plus + MyBatis XML | CRUD 与复杂 SQL 分别使用 |
| 连接池 | HikariCP | 每服务独立配置 |
| 数据迁移 | Flyway | 按服务独立 Schema 与迁移历史 |
| 缓存与锁 | Redis + Redisson | 不作为业务权威数据源 |
| 消息队列 | RabbitMQ | 异步事件、重试、幂等 |
| 调度 | XXL-JOB | 要求 M0 就部署 |
| API 契约 | OpenAPI 3 | 前端生成类型化客户端 |
| 日志和监控 | SLF4J / Logback、Actuator、Micrometer、OpenTelemetry | 日志与链路标识标准化 |
| Java 测试 | JUnit 5、Mockito、Testcontainers | 覆盖租户隔离和数据边界 |
| 容器 | Docker + Docker Compose | 第一阶段使用，生产高可用另外设计 |

### 2.2 数据存储

| 类型 | 产品 | 数据职责 |
| --- | --- | --- |
| 业务事务数据 | **MySQL 8.0** | 租户、用户、组织、商品、物料、BOM、成本、订阅、营销业务 |
| 经营分析 + 向量检索 | **Apache Doris 4.x** | 销售分析、经营指标、分析读模型、知识向量、语义检索 |
| 可选向量后端 | Milvus | Doris 检索性能/功能经验证不满足要求时引入 |
| 缓存 | Redis | 可重建缓存、会话辅助状态、分布式协调 |
| 事件通道 | RabbitMQ | 异步流程和领域事件 |
| 文件与模型 | S3 兼容对象存储 | 图片、附件、导出文件、模型文件 |

**要求：** 业务写操作先落 MySQL，再使用可靠的同步管道更新 Doris 读模型或知识索引。AI 的经营指标问答优先使用受控 Doris SQL，文档知识问答使用向量检索；两类检索都必须执行可信的租户和业务权限过滤。

### 2.3 前端与移动端

| 层级 | 技术选型 |
| --- | --- |
| Web | Vue 3 + TypeScript |
| 管理后台模板 | Arco Design Pro Vue |
| UI 组件 | Arco Design Vue |
| **唯一默认图标库** | Arco Design Vue 官方 Icons |
| **字体** | MiSans（合法获取与接入，遵守许可/署名要求） |
| 路由 | Vue Router 4 |
| 状态管理 | Pinia |
| 构建 | Vite |
| 包管理 | pnpm Workspace |
| 图表 | Apache ECharts |
| HTTP | Axios + OpenAPI 生成的类型化客户端 |
| 测试 | Vitest + Playwright |
| 移动端 | uni-app x + Vue 3（独立工程，使用统一设计语言） |

### 2.4 AI 技术

- Python + FastAPI 作为 AI/预测服务基础。
- 预测候选模型包括 CatBoost、LightGBM、XGBoost 等，以回测/业务指标确定实际模型。
- 预测训练和推理按需部署独立 Worker，不与在线 Java 业务服务争抢计算资源。
- Spring AI 或标准大模型 API 适配层按实际业务接入。
- Doris 4.x 默认承载 RAG 向量检索，预留 Milvus 可插拔接口。

---

## 3. 五服务架构与职责边界

### 3.1 服务列表

| 工程名称 | 类型 | 职责 | 独立部署 |
| --- | --- | --- | --- |
| `lingjuli-gateway` | Spring Boot / Gateway | API 统一入口、路由、限流、认证预校验、租户上下文 | 是 |
| `lingjuli-iam` | Spring Boot | 身份、账号、凭据、角色、权限、会话、禁用失效 | 是 |
| `lingjuli-platform` | Spring Boot | 租户、套餐、订阅、应用开通、资源配额、平台运营 | 是 |
| `lingjuli-business` | Spring Boot + Maven 内部模块 | 组织、商品、BOM、成本、巡检、分析、DTC 等 | 是 |
| `lingjuli-integration` | Spring Boot | 第三方连接器、同步任务、字段映射、对账、重试 | 是 |
| `lingjuli-common` | Maven 公共模块组 | Core、Web、Security、Tenant、MyBatis、Redis、MQ 等 | **否** |
| `lingjuli-contracts` | Maven 契约模块 | 跨服务接口/事件契约，不包含共享业务实体或 Mapper | 否 |

> **五服务**指五个独立运行与部署的应用，不包含 `lingjuli-common`。多个 Maven 模块可打包到同一个 `*-app` 进程。

### 3.2 Business 内部模块规划

| Maven 模块 | 业务范围 | 计划阶段 |
| --- | --- | --- |
| `business-organization` | 集团、区域、品牌、业态、门店、外部组织映射 | 第一批 |
| `business-product` | 商品类型、品类、分类、商品、零售商品 | 第一批 |
| `business-material` | 物料、BOM、出成率、过敏原 | 第二批 |
| `business-cost` | 菜品成本、成本计算、成本分析 | 第二批 |
| `business-operation` | 任务、检查、巡检、整改闭环 | 第二批 |
| `business-workforce` | 员工与岗位业务、人力计划、工时、排班 | 第三批 |
| `business-analytics` | 经营指标、销售分析、驾驶舱、报表 | 第三批 |
| `business-forecast` | 销售、菜品和人力预测任务/结果/确认 | 第三批 |
| `business-dtc` | 会员、标签、客群、营销活动与触达 | 第三批 |
| `business-ai` | 店长助手、知识检索、AI 编排 | 后续扩展 |

#### 首批重要业务规则

**组织主数据**：同庆云经营组织与金蝶法人/仓库、人力管家部门/班组/员工等外部组织分别建模，通过映射关系、来源 ID 和变更历史关联；不得直接合并为一张 `sys_dept` 树。

**商品域**：采用三级结构“商品类型 → 商品品类 → 商品分类”，保留商品分类原有业务字段，支持 `DTC适用`、零售商品编码关联、下级绑定校验与删除限制。

**业务流程**：第三方同步由 Integration 接入与记录，正式业务入库通过 Business 领域接口或事件完成，避免连接器直接写业务主表。

---

## 4. 工程结构（目标目录）

> 下列为目标结构。Codex 应按里程碑逐步创建实际需要的文件，不要在 M0 一次生成所有空模块。

```text
lingjuli-operation/
├── AGENTS.md                       # 全局 Codex 开发入口
├── README.md                       # 项目说明
├── .gitignore
├── .editorconfig
│
├── docs/                           # 目录、文件名与正文均使用中文
│   ├── 01-产品与需求/
│   ├── 02-技术架构/
│   ├── 03-界面设计/
│   ├── 04-测试与质量/
│   ├── 05-环境与交付/
│   └── 06-Codex协作/
│
├── backend/
│   ├── pom.xml                     # Maven 聚合工程，仅放在 backend
│   ├── AGENTS.md
│   ├── lingjuli-common/            # 公共 Maven 模块，不单独部署
│   │   ├── common-core/
│   │   ├── common-web/
│   │   ├── common-security/
│   │   ├── common-tenant/
│   │   ├── common-mybatis/
│   │   ├── common-redis/
│   │   └── common-mq/
│   ├── lingjuli-contracts/         # API/事件契约
│   ├── lingjuli-gateway/
│   ├── lingjuli-iam/
│   │   ├── iam-app/
│   │   ├── iam-auth/
│   │   ├── iam-user/
│   │   └── iam-permission/
│   ├── lingjuli-platform/
│   │   ├── platform-app/
│   │   ├── platform-tenant/
│   │   ├── platform-subscription/
│   │   └── platform-operation/
│   ├── lingjuli-business/
│   │   ├── business-app/
│   │   ├── business-organization/
│   │   ├── business-product/
│   │   ├── business-material/
│   │   ├── business-cost/
│   │   ├── business-operation/
│   │   ├── business-workforce/
│   │   ├── business-analytics/
│   │   ├── business-forecast/
│   │   ├── business-dtc/
│   │   └── business-ai/
│   └── lingjuli-integration/
│       ├── integration-app/
│       ├── integration-core/
│       ├── integration-kingdee/
│       ├── integration-hr/
│       ├── integration-pos/
│       └── integration-wecom/
│
├── frontend/
│   ├── package.json                # Node 构建入口，仅放在 frontend
│   ├── pnpm-workspace.yaml         # pnpm Workspace，仅放在 frontend
│   ├── pnpm-lock.yaml              # 安装依赖后提交锁文件
│   ├── AGENTS.md
│   ├── apps/
│   │   ├── design-lab/
│   │   ├── platform-web/
│   │   └── merchant-web/
│   └── packages/
│       ├── design-tokens/
│       ├── ui-components/
│       ├── api-client/
│       └── shared-utils/
│
├── mobile/
│   └── lingjuli-app/               # uni-app x
├── ai/
│   └── forecast-engine/            # Python FastAPI / 模型任务
├── tests/
│   ├── e2e/
│   ├── visual/
│   ├── contracts/
│   └── fixtures/
├── config/
│   ├── env/
│   └── ssh/
├── deploy/
│   ├── compose/
│   ├── nginx/
│   └── monitoring/
└── scripts/
```

**目录约束：** 仓库根目录不需要 Maven 或 pnpm 聚合构建文件；`pom.xml` 属于 `backend/`，`package.json` 与 `pnpm-workspace.yaml` 属于 `frontend/`。单个应用或包可以继续有自己的构建配置。

---

## 5. 中文文档与 Codex 约束体系

### 5.1 六类文档

| 目录 | 文档职责 | 推荐核心文件 |
| --- | --- | --- |
| `01-产品与需求` | 业务目的、范围、规则、字段、权限、异常、验收 | `产品定位与目标.md`、`功能模块规划.md`、`业务术语字典.md`、`需求文档模板.md`、功能 PRD |
| `02-技术架构` | 技术基线、服务与数据边界、安全、API/事件 | `技术栈与版本基线.md`、`系统架构与服务边界.md`、`数据存储与集成规范.md`、`多租户与权限规范.md`、ADR |
| `03-界面设计` | 设计系统、组件、布局、交互、视觉验收 | `设计系统总则.md`、`视觉变量规范.md`、`导航与布局规范.md`、`查询区域规范.md`、`表格与弹窗规范.md` |
| `04-测试与质量` | 单测、集成、E2E、视觉回归、安全与 DoD | `测试策略与要求.md`、`测试用例模板.md`、`UI视觉验收规范.md`、`功能完成标准.md` |
| `05-环境与交付` | Local/Dev/Test/Prod、SSH、发布、恢复 | `多环境配置规范.md`、`开发环境搭建.md`、`部署发布与回滚规范.md` |
| `06-Codex协作` | AI 执行规则、任务交接、决策与变更 | `Codex开发执行流程.md`、`任务模板.md`、`任务管理与交接.md`、`决策与变更规则.md` |

**规范：** `docs` 内全部使用中文目录名、中文 Markdown 文件名和中文正文。涉及代码、依赖、接口、数据库字段等技术标识时保留英文标识。

### 5.2 AGENTS.md 层级

| 文件 | 职责 |
| --- | --- |
| `/AGENTS.md` | 全局执行流程、文档引用、风险等级、禁止事项、交付要求 |
| `/backend/AGENTS.md` | Java、微服务、服务边界、Schema、租户权限、迁移、测试 |
| `/frontend/AGENTS.md` | Vue/Arco、Design Tokens、组件复用、视觉和交互验收 |

`AGENTS.md` 是规则入口，不复制 PRD、设计 Token 具体数值或技术依赖版本清单；一个事项应当有唯一权威定义。

### 5.3 Codex 的基本工作流程

1. 读取当前目录适用的 `AGENTS.md` 与任务所需权威规范。
2. 读取已批准的需求文档、权限边界与验收标准。
3. 明确修改范围、影响分析、API/数据库/组件变更计划。
4. 分批实现，优先复用现有公共模块和 Design System。
5. 执行编译、静态检查、单元、集成、E2E 和视觉验证。
6. 输出变更文件、测试结果、未完成项、风险与恢复方案。
7. 经人工确认后合并，并更新项目进度与必要文档。

以下情形未经授权不得执行：改变五服务架构或数据库所有权、修改租户安全模型、直接连接或更改生产库、危险数据清理、擅自更改设计主色/字号体系、以 Mock 假装真实功能已完成。

---

## 6. Lingjuli Design System

### 6.1 设计目标与当前状态

**设计方向：** 轻量现代型、年轻化、克制蓝灰色、少阴影、适合高信息密度企业 SaaS。字体统一 MiSans，图标统一使用 Arco Design Vue 官方 Icons。

Lingjuli Design System V1.0 当前处于**候选基线**状态，是 Web UI 的实施基础；完成真实 Vue 编译、MiSans 合法接入及人工批准的视觉回归后方可正式冻结。后续设计变更必须及时回写至 Design System 权威文档，并同步更新相应 Token、组件样板和视觉回归基准；业务页面不得自行形成未记录的设计分支。

### 6.2 视觉 Token 初步基线

| 项目 | 初步固定值或规则 |
| --- | --- |
| 品牌主色 | `#165DFF` |
| 页面背景 | `#F7F8FA` |
| 内容背景 | `#FFFFFF` |
| 主文字 | `#1D2129` |
| 默认边框 | `#E5E6EB` |
| 字体 | MiSans |
| 常规输入高度 | 32px |
| 默认表格行高 | 46px |
| 普通查询搜索框 | 220px |
| 普通下拉框 | 190px |
| 组织/门店选择 | 230px |
| 日期范围 | 320px |
| 导航展开宽度 | 224px |
| 顶部导航高度 | 56px |
| 默认阴影 | 无 |
| 焦点态 | 品牌蓝边框、无阴影；保留可见键盘焦点 |

以上尺寸以 Design System V1.0 为基线；后续调整须经设计确认后更新至权威文档、Token 和 Design Lab 黄金样板，各页面不得自行设置第二套 Token。

### 6.3 导航规范

- 目录：图标 + 文字 + 展开箭头。
- 二级菜单：**纯文字、无图标、无按钮块、无额外缩进**；文字对齐以最终组件 Token 为准。
- 二级菜单选中态：浅背景 + 蓝色文字，**无左侧蓝色竖线**。
- 不额外显示“工作空间 / 业务管理 / 系统设置”等特殊分组标题。
- 图标的唯一默认来源是 Arco Design Vue 官方 Icons，特殊业务图标需审核后进入统一图标映射。

### 6.4 查询区域规范

- 默认字段与“展开”后新增字段使用**同一连续布局**，不拆成两个查询区。
- 查询、重置、展开/收起按钮始终按视觉顺序位于最后一组条件之后，空间不足时整体换行。
- 桌面搜索、下拉、日期框设置固定建议宽度与最大宽度，**禁止单个查询框无上限铺满整行**。
- 输入及下拉 Hover/Focus 不产生阴影。
- 下拉选项采用定制化统一样式，正式实现以 Arco `a-select` 等组件为基础，不另造独立组件库。
- 组织树支持层级/搜索/权限禁用；多选支持数量摘要；日期范围支持快捷范围、起止校验及跨月。

### 6.5 表格、工具栏、弹窗与状态

- 工具栏：左侧主操作/批量操作，右侧刷新、列设置、导出；操作优先级统一。
- 行操作：编辑等高频动作突出，启用/停用使用中性语义，删除使用危险语义；低频操作可收入“更多”。
- 分页：总数、当前范围、每页条数、页码切换；状态与表格保持一致。
- 弹窗宽度建议：标准表单 560px，确认弹窗 420px，复杂弹窗 720px；右侧详情抽屉 520px。
- 表单包含必填、唯一性、禁用、提交中、未保存关闭提醒等完整状态。
- Loading、Empty、Error、No Permission 必须区分；不能用“暂无数据”冒充接口失败。
- 按钮展示并测试 Default、Hover、Focus、Loading、Disabled、Danger 等状态。

### 6.6 Design Lab 菜单

| 一级菜单 | 展示内容 |
| --- | --- |
| 总览 | 设计系统入口与当前版本 |
| 页面模板 | 标准列表、详情、表单、驾驶舱、登录 |
| 查询与筛选 | 基础与高级查询、树形、多选、远程搜索、日期范围 |
| 数据展示 | 表格、工具栏、行操作、分页、Loading/Empty/Error |
| 反馈与浮层 | Message/Notification、确认、标准弹窗、复杂弹窗、抽屉 |
| 组件状态与交互演示 | Hover、Focus、Loading、Disabled、Error 等 |
| 基础视觉规范 | 字体、颜色、间距、图标、布局 |

**Golden Pages 验收：** 在 MiSans 正式接入、依赖版本固定和浏览器环境一致的情况下，对 1280/1440/1920px 关键页面进行 Playwright 截图、交互和溢出检查，人工审核后固定基准。

---

## 7. 数据隔离、数据库归属与四环境

### 7.1 MySQL Schema 分配

每个数据所属服务拥有独立 Schema、运行时账号、Flyway 迁移记录：

| 服务 | **提议的** Schema | 决策状态 |
| --- | --- | --- |
| IAM | `lingjuli_iam` | **已确认** |
| Platform | `lingjuli_platform` | **已确认** |
| Business | `lingjuli_business` | **已确认** |
| Integration | `lingjuli_integration` | **已确认** |
| Gateway | 无业务 Schema | 确定原则 |

**Schema 前缀：** 统一使用 `lingjuli_`。历史业务库在经过备份确认与明确授权后方可清理；新工程与新增迁移一律使用 `lingjuli_` 前缀。

**多租户存储模型：** 各服务使用共享 Schema；除明确列入全局字典、平台级元数据且经评审豁免的表外，所有租户业务表必须包含 `tenant_id`。所有业务查询、更新、删除、唯一索引与数据权限校验必须纳入 `tenant_id`，禁止仅依赖前端传参或网关校验。

**商户号：** 平台租户使用五位数字 `tenant_code` 作为商户号，归属 `lingjuli_platform.platform_tenant`；商家端登录必须提交商户号，IAM 仅在认证成功后的服务端会话中保留对应 `tenant_id`。

**权限边界：** 平台角色与商户组织角色严格隔离。平台角色仅用于平台运营、租户、套餐和订阅等平台级能力；商户组织角色仅用于租户内的组织与业务数据范围授权。任何平台级身份访问商户数据均须采用明确、可审计的授权路径。

### 7.2 环境矩阵

| 环境 | 用途 | 连接约束 |
| --- | --- | --- |
| Local | 个人开发、自测 | 本地 MySQL、Redis 等 |
| Dev | 共享开发联调 | 京东云开发库；本机开发经 SSH 隧道，服务端使用内网 |
| Test | 版本验收、集成测试 | 京东云独立测试库；隔离于 Dev |
| Production | 正式租户业务 | 仅生产部署环境通过内网访问，不允许开发机任意切换直连 |

服务采用 Spring Profiles 配置：

```text
application.yml
application-local.yml
application-dev.yml
application-test.yml
application-prod.yml
```

### 7.3 SSH 隧道与安全约束

- 每台电脑/每位开发者使用独立 SSH Key 与受控 SSH 账号；SSH 私钥不提交 Git。
- 通过统一别名（如 `lingjuli-dev` / `lingjuli-test`）和本机端口转发访问远端测试数据库。
- 远端 MySQL 3306 不直接面向公网开放。
- 本地连接测试库使用本机隧道地址；京东云容器内的服务连接内网数据库地址，**不得沿用开发机 `127.0.0.1` 隧道配置**。
- 密码和生产内网地址从环境变量、Nacos 与专门的密钥管理流程提供；生产密钥不得存于仓库。
- Codex 默认只操作 Local，Dev/Test 远端连接与结构迁移需明确授权，Production 只能通过正式审核的发布流程修改。
- 必须具备数据库备份、恢复演练、最小权限、审计与迁移审批。

### 7.4 Nacos 配置与密钥分发

- 服务的数据库连接配置由 Nacos 按“服务名 + 环境”分发，Data ID 统一命名为 `${spring.application.name}-${spring.profiles.active}.yml`，例如 `lingjuli-business-dev.yml`；不得继续使用与本项目无关的服务名。
- 数据库地址、端口、库名、连接池参数、超时和功能开关等非敏感配置可写入对应的普通 Data ID。
- 数据库密码、AI 模型 API Key、第三方平台 `AccessKey` / `SecretKey` 等敏感值不得以明文保存。当前阶段使用 Nacos 配置加密插件作为过渡方案，保存于独立的加密 Data ID，例如 `cipher-aes-lingjuli-business-dev-secret.yml`；后续具备条件时迁移至企业 KMS 或密钥管理服务。
- Nacos 配置加密不替代鉴权、TLS 和网络隔离。加密主密钥及 Nacos 自身访问凭据必须由部署环境变量或密钥管理服务注入，禁止存回 Nacos 或提交 Git。
- 每个环境使用独立 Nacos Namespace：`lingjuli-local`、`lingjuli-dev`、`lingjuli-test`、`lingjuli-prod`；配置 Group 统一使用 `LINGJULI`，不使用默认组。生产 Namespace 的修改权限须受正式发布流程控制。
- 应用本地仅允许保留连接 Nacos 所需的最小引导信息（Nacos 地址、Namespace、访问凭据的环境变量引用）；本地 `.env.local` 必须忽略提交，只提交不含真实值的 `.env.example`。
- 每个服务使用独立数据库运行账号，权限仅覆盖该服务拥有的 Schema。密码轮换必须经过“更新密钥 → 灰度发布或重启服务验证 → 废弃旧密码”的流程并留存记录。

### 7.5 数据模型与同步规则

- 业务核心写入 MySQL，分析数据经可靠同步进入 Doris。
- RabbitMQ 消费者必须支持重试与幂等；重要事务事件采用 Outbox 等可靠发布方案。
- 读 Doris、查询知识向量时同样校验租户和数据授权范围。
- 每张业务表指定唯一拥有者；同一个 MySQL 实例不意味着不同服务可以任意跨 Schema JOIN 或写入。

---

## 8. 测试策略与完成标准

| 测试层级 | 核心验收 |
| --- | --- |
| 单元测试 | 领域规则、状态机、基础工具、Vue 组件 |
| 集成测试 | MySQL/Redis/RabbitMQ 集成、迁移、数据幂等 |
| 合同测试 | OpenAPI 请求响应与跨服务事件契约 |
| 安全测试 | 租户隔离、角色、组织数据权限、套餐到期、账号禁用 |
| E2E 测试 | 登录、查询、增删改、弹窗、筛选、分页等用户路径 |
| UI 视觉回归 | MiSans、Token、组件状态、1280/1440/1920px Golden Pages |
| 发布检查 | 配置、备份、迁移、回滚、监控、灰度与审计 |

**功能完成定义（Definition of Done）：** 已批准需求的全部验收条件通过；编译、类型检查、Lint 和必要测试完成；安全与数据库变更得到确认；有可复现的测试记录。未真正执行的测试必须明确标记为“未运行”，不得宣称通过。

---

## 9. 分阶段建设与交付

| 里程碑 | 核心范围 | 退出标准 |
| --- | --- | --- |
| **M0 工程初始化与 Design System** | Monorepo、中文文档、三级 AGENTS、前端 Workspace、Design Lab、Tokens、公共组件 | Vue/Arco 真实编译、设计样板人工审核、基础视觉测试通过 |
| **M1 SaaS 核心底座** | 五个 Spring Boot 服务、Gateway、Nacos、MySQL、Redis、Flyway、IAM、租户及套餐基础权益 | 两个测试租户独立登录和运行，无法越权读取对方数据 |
| **M2 组织与商品中心** | 经营组织、外部组织映射、商品类型/品类/分类、商品主数据 | 能从商户维度管理组织、商品和关联关系，包含删除/权限校验 |
| **M3 餐饮运营能力** | 金蝶/人力管家同步、物料/BOM、过敏原、成本、任务巡检、基础经营报表 | 外部同步至业务应用完整闭环，错误重试和权限检验通过 |
| **M4 智能经营与商业扩展** | Doris 分析、预测、人力排班、DTC、AI 助手、移动端、商业订阅增强 | 按套餐提供功能和计量，预测/营销有可追踪执行结果 |

**实施说明：** M1 就必须具备基础租户与套餐权益，不能等到 M4 才实现商业 SaaS 的核心控制。M4 增强订阅运营、计量和业务产品矩阵。

---

## 10. 正式开工前的待确认事项

| 序号 | 事项 | 当前建议 | 状态 |
| --- | --- | --- | --- |
| 1 | 工程根目录 | `lingjuli-operation` | **已确认** |
| 2 | 后端公共模块 | `lingjuli-common`，非独立服务 | **已确认** |
| 3 | 五服务 + Maven 多模块 | Gateway / IAM / Platform / Business / Integration | **已确认** |
| 4 | Java、业务库、分析/向量 | Java 17、MySQL 8.0、Doris 4.x；Milvus 可选 | **已确认** |
| 5 | Web | Vue 3 + Arco Design Pro Vue + MiSans | **已确认** |
| 6 | 图标 | Arco 官方 Icons 唯一默认来源 | **已确认** |
| 7 | docs 文档 | 中文目录、文件名与正文 | **已确认** |
| 8 | 开发环境 | Local / Dev / Test / Production + SSH 隧道 | **已确认** |
| 9 | Design System 交付基线 | Design System V1.0 作为 UI 实施基础；后续变更同步更新权威文档、Token 与视觉基准 | **已确认** |
| 10 | 数据库 Schema 前缀 | 统一 `lingjuli_`；历史业务库仅在备份确认与明确授权后清理 | **已确认** |
| 11 | 移动端 | uni-app x 独立工程，单独设计与开发 | **已确认** |
| 12 | Git/CI 平台 | 使用用户个人 GitHub；采用 Pull Request 与流水线 | **已确认** |
| 13 | Java/前端依赖补丁版本 | 已通过初始构建验证并锁定；详见《初始依赖版本基线 V0.1》及 `pnpm-lock.yaml` | **已确认** |
| 14 | 多租户存储模型 | 共享 Schema；租户业务表统一使用 `tenant_id` | **已确认** |
| 15 | 权限边界 | 平台角色与商户组织角色隔离 | **已确认** |
| 16 | Nacos 配置与密钥 | Nacos 3.2.4；环境 Namespace + `LINGJULI` Group；使用加密配置过渡 | **已确认** |

---

## 11. 正式启动 Codex 的建议

1. **首先执行 M0。** 建立最终认可的工程结构、中文文档、Codex 规则与前端 Design Lab。
2. 优先修复和运行 V0.9.1 源码，接入 MiSans，完成真实 Vue/Arco 组件和 Playwright 视觉基准。
3. M0 验收通过后，开始 M1 的五服务后端基础工程与数据库安全接入。
4. M1 通过多租户/权限/会话测试后，开始 M2 的组织主数据与商品中心。
5. 后续每个里程碑必须能在受控环境独立验收，不得一次性生成全部业务而跳过验证。

> **最终实施建议：** 采用“业务单体内部模块化、平台基础微服务化、计算任务独立化”的架构，以 MySQL 8.0 + Doris 4.x 为核心，Vue 3 + Arco Design Pro 和 Lingjuli Design System 统一前端。先交付可运行的工程与质量基线，再逐阶段开发餐饮 SaaS 业务。

---

*本文件为总体审阅与决策基线，不代替后续各功能 PRD、API 契约、具体数据库 DDL、部署脚本及 UI 组件实现。*
