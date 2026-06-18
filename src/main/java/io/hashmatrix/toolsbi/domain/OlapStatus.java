package io.hashmatrix.toolsbi.domain;

/**
 * 取数基础设施连通快照：本仓工程基座对 Doris(OLAP) 的可达性自检结果。
 *
 * @param tenantCatalog 当前租户隔离取数库（见 {@link TenantCatalog}）
 * @param doris         Doris 是否可达（OLAP 取数引擎，MySQL 协议探针）
 */
public record OlapStatus(String tenantCatalog, boolean doris) {

    /** 取数基础设施可达。 */
    public boolean healthy() {
        return doris;
    }
}
