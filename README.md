# 同庆云 SaaS

面向连锁餐饮企业的智能经营 SaaS 平台。

项目当前处于 M0 工程初始化阶段。总体架构、技术选型与里程碑请参阅 [项目总体架构与工程规划](docs/02-技术架构/同庆云%20SaaS%20项目总体架构与工程规划%20V1.0.md)。

## 当前目录

- `docs/`：中文项目文档与决策记录。
- `backend/`：后端 Maven 聚合工程；将在 M1 创建。
- `frontend/`：pnpm Workspace、Design System 与 Web 应用。
- `tests/`：跨端 E2E、视觉与契约测试。
- `config/`：不含真实密钥的环境配置样例。

## 安全约束

真实密码、AI API Key、SSH 私钥和生产地址不得提交 Git。敏感运行时配置通过 Nacos 加密 Data ID 分发；本地仅使用未提交的环境变量文件。

## 本地构建

```powershell
cd backend
mvn -DskipTests verify

cd ../frontend
pnpm install
pnpm build
```

前端应用分别位于 `frontend/apps/design-lab`、`frontend/apps/platform-web` 和 `frontend/apps/merchant-web`。移动端工程由项目负责人独立创建，不在本仓库初始化范围内。

