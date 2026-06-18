package io.hashmatrix.toolsbi.infra;

import io.hashmatrix.starter.tenant.TenantContextHolder;
import io.hashmatrix.toolsbi.domain.OlapStatus;
import io.hashmatrix.toolsbi.domain.TenantCatalog;
import io.hashmatrix.toolsbi.domain.port.OlapConnectivityPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * {@link OlapConnectivityPort} 的 Doris(OLAP, MySQL 协议) 适配实现。
 *
 * <p>多租户隔离取数：按当前 {@link TenantContextHolder} 派生 {@link TenantCatalog}（{@code bi_<tenant>}），
 * 作为后续取数的库限定目标——BI / 大屏一律在该租户库内取数，绝不跨租户串。租户库由 {@code control-plane}
 * 编排开通；本基座只**解析路由目标 + 验证连通**，不 {@code USE} 可能尚未开通的租户库、也不建业务报表。
 *
 * <p>实际取数落地时应以租户库限定查询（{@code SELECT ... FROM bi_<tenant>.<table>}，或在该租户库内
 * 建会话后查询），租户名已在 {@link TenantCatalog} 归一化（无注入）。这是报表/分析查询应复用的隔离范式。
 */
@Component
public class DorisConnectivityAdapter implements OlapConnectivityPort {

    private static final Logger log = LoggerFactory.getLogger(DorisConnectivityAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public DorisConnectivityAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OlapStatus probe() {
        TenantCatalog catalog =
                TenantCatalog.forTenant(TenantContextHolder.getTenantId().orElse("public"));
        return new OlapStatus(catalog.name(), dorisReachable());
    }

    private boolean dorisReachable() {
        try {
            // 连通探针：Doris FE 兼容 MySQL 协议，SELECT 1 验证可达（不依赖任何业务库表）
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(one);
        } catch (RuntimeException ex) {
            log.warn("Doris 连通探测失败: {}", ex.getMessage());
            return false;
        }
    }
}
