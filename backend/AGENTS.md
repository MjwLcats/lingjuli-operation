# 后端规则

- 后端 Maven 聚合根固定为 `backend/pom.xml`，不得在仓库根目录新增 Maven 聚合文件。
- M1 前不要批量创建五服务或业务领域空模块；按已批准里程碑逐步创建。
- IAM、Platform、Business、Integration 分别拥有 `lingjuli_iam`、`lingjuli_platform`、`lingjuli_business`、`lingjuli_integration` Schema 与独立运行账号。
- 每个服务独立维护 Flyway 迁移历史；禁止跨 Schema 写入和跨服务直接修改业务数据。
- 数据库密码、AI Key 和第三方凭据只可通过 Nacos 加密配置或部署环境注入。
- 所有租户数据访问均须在服务端执行 `tenant_id` 与组织权限校验。

