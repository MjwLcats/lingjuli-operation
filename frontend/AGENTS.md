# 前端规则

- 前端 pnpm Workspace 根固定为 `frontend/`；不得在仓库根目录新增 `package.json` 或 `pnpm-workspace.yaml`。
- Web 页面使用 Vue 3、TypeScript、Arco Design Vue 与 Lingjuli Design System V1.0。
- 优先复用 `packages/design-tokens` 和 `packages/ui-components`；禁止在页面中另建未记录的 Token。
- 默认图标仅使用 Arco Design Vue 官方 Icons；字体采用获准接入的 MiSans。
- 每次调整 Design System，必须同步更新 `docs/03-界面设计/` 的权威文档与视觉基准。

