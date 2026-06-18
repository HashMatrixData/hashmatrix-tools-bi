/**
 * 数据工具（BI）分系统 —— 分层骨架（依赖方向自上而下，内层不依赖外层）：
 *
 * <ul>
 *   <li>{@code api} —— 入站适配（REST 控制器）：透传租户、统一返回（starter-web），不含业务规则。</li>
 *   <li>{@code app} —— 应用服务：编排用例、事务边界、审计（starter-audit），依赖 domain 端口。</li>
 *   <li>{@code domain} —— 领域模型与出站端口（port）：纯业务，无框架依赖。</li>
 *   <li>{@code infra} —— 出站适配：实现 domain 端口，对接 Doris(OLAP, MySQL 协议)、按租户隔离取数路由。</li>
 * </ul>
 *
 * <p>本基座只打通取数基础设施与多租户隔离取数；报表/自助分析/可视编排业务、以及 DataEase 集成
 * （SSO + 租户上下文透传 + 部署级品牌外框，见 {@code docs/integration/dataease.md}）在本基座之上落地。
 */
package io.hashmatrix.toolsbi;
