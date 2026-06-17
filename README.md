# hashmatrix-tools-bi

> hashmatrix 数据中台子模块 · 所属：应用服务层 · 数据工具分系统（BI）
>
> 主仓：[HashMatrixData/hashmatrix](https://github.com/HashMatrixData/hashmatrix)

## 角色与位置（一眼看懂）

- **所属**：应用服务层 · 数据工具（BI）分系统。
- **一句话**：把数据"看得见"——报表 / 自助分析 / 可视化 / 可视化编排。
- **调用流**：data-foundation（Doris 取数）→ **tools-bi（报表/分析/编排）** → 大屏与图表在 webui 呈现。

## 职责与边界

- **做**：报表、自助分析、70+ 图表可视化、可视化编排的后端能力与集成。
- **不做（边界）**：数据大屏**编辑器前端**在 `webui`；重型 BI 报表设计器**集成 DataEase** 而非自研；取数依赖 `data-foundation`（Doris）。

## 骨架技术选型（首选 · 待逐仓细化）

| 维度 | 选型 |
|--|--|
| 运行时 | Spring Boot（Java / TS） |
| BI / 报表 | **DataEase**（集成纳入，备 Superset） |
| 取数 / OLAP | **Doris** |

> 大屏编辑器与图表前端在 `webui`（AntV）；集成产品保持租户上下文 + 部署级品牌外框。

## 产品形态与多租户（北极星）

**双模交付**：公网 SaaS（我们运营 · 统一**我们品牌** · 租户=企业）／私有化部署（客户环境 · **客户品牌**部署级 · 租户=客户部门）。品牌**部署级**、不按租户运行期换肤。多租户走 **C 分层桥接**：控制平面共享 + 数据平面按租户隔离（Keycloak Organizations 单 realm · schema/db-per-tenant · namespace-per-tenant），由 `control-plane` 编排开通。

**本仓视角**：BI / 大屏按租户隔离取数；集成产品保持租户上下文 + 部署级品牌外框。

> 详见主仓 `docs/00-主仓初始化-spec.md`、`docs/architecture/05-多租户与控制平面.md`。

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/tools-bi`。架构背景见主仓 `docs/architecture/`。

## License

Apache-2.0
