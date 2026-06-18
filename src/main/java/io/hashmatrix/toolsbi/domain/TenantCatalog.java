package io.hashmatrix.toolsbi.domain;

/**
 * 租户取数隔离的 Doris 数据库（catalog）名值对象（多租户 = db-per-tenant，见架构 05 §5）。
 *
 * <p>由租户标识派生 {@code bi_<sanitized>}：仅保留 {@code [a-z0-9_]}、其余折叠为 {@code _}，
 * 确保可安全用于库名限定（杜绝注入）。BI / 大屏按此库隔离取数，绝不跨租户串。
 * 租户库由 {@code control-plane} 编排开通；本基座只解析路由目标，不建业务库表。
 *
 * @param name 归一化后的库名，形如 {@code bi_acme}
 */
public record TenantCatalog(String name) {

    /** 库名前缀：标识数据工具分系统的租户取数库。 */
    public static final String PREFIX = "bi_";

    public TenantCatalog {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("catalog name must not be blank");
        }
    }

    /**
     * 由租户标识派生隔离取数库。
     *
     * @param tenantId 租户标识，非空白
     * @return 归一化取数库
     */
    public static TenantCatalog forTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        String sanitized = tenantId.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        return new TenantCatalog(PREFIX + sanitized);
    }
}
