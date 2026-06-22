> ⚠️ **已废弃 / DEPRECATED（2026-06）** — 本仓所属「数据工具(BI)」分系统已**移出平台建设范围**：BI/数据可视化是数据治理**之上**的数据消费层，不由本平台承载（私有化客户多自带 BI、平台只供数），改由上层独立 BI 产品（DataEase 类）经数据服务对接。详见主仓架构决策 **AD-19**（`docs/architecture/README.md`）。本仓已归档（只读）。

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

## 工程基座（本地构建与运行）

> 本仓已具备**可独立开发/编译/调试/运行**的 Spring Boot 工程基座。
> 本基座只打通取数基础设施与多租户隔离取数，**不实现报表/分析业务**；
> 报表设计器**集成 DataEase**（非自研，见 `docs/integration/dataease.md`），大屏/图表前端在 `webui`。

**分层骨架**（依赖自上而下，内层不依赖外层）：`api`（REST/统一返回）→ `app`（用例编排/审计）→ `domain`（领域模型+出站端口）→ `infra`（Doris 适配+租户隔离取数路由）。

**公共能力复用**（经 Maven 坐标引用 `libs-java`，非 submodule 路径）：
`starter-tenant`（`X-Tenant-Id` → `TenantContext`）、`starter-web`（统一返回/异常）、`starter-audit`（结构化审计·自动加盖租户）、`starter-observability`（actuator + `/actuator/prometheus`）、`starter-test`（JUnit5/AssertJ/Mockito/Testcontainers + 脱敏 fixtures）。

```bash
# 1) 纯打包（跳过所有测试，无需 Docker）—— 对应 DoD 验收
mvn -q -DskipTests package

# 2a) 构建 + 单测（surefire，无需 Docker）
mvn -B package

# 2b) + 集成切片（failsafe，Testcontainers 起 MySQL 作 Doris 协议替身，需 Docker）
mvn -B verify

# 3) 本地起栈（真实 Apache Doris）+ 运行，验证健康检查
docker compose -f docker-compose.local.yml up -d
bash scripts/run-local.sh          # 或 mvn spring-boot:run -Dspring-boot.run.profiles=local
curl -s localhost:8080/actuator/health
curl -s localhost:8080/api/tools-bi/probe -H 'X-Tenant-Id: tenant-demo'
```

> 经 Maven 坐标解析 `io.hashmatrix` 公共依赖需能访问 GitHub Packages（`~/.m2/settings.xml` 配置 `server id=github` + `GITHUB_TOKEN`，`packages:read`）。
> **取数选型**：Doris(OLAP) 走 MySQL 协议（JDBC）；集成切片用 mysql:8 协议替身保证 CI 稳定，本地完整体验用真实 Doris。
> **多租户隔离取数**：每租户路由到 `bi_<tenant>` 取数库（由 control-plane 编排开通）。
> **可观测**：Actuator + `/actuator/prometheus`；OTel 链路走部署期 Java agent（不绑代码）。
> **连接参数/凭据均 env 可覆盖、不入库**（红线合规）。

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/tools-bi`。架构背景见主仓 `docs/architecture/`。

## License

Apache-2.0
