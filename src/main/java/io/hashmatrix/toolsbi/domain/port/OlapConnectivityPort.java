package io.hashmatrix.toolsbi.domain.port;

import io.hashmatrix.toolsbi.domain.OlapStatus;

/**
 * 取数连通端口（领域出站端口，六边形架构）：屏蔽 Doris(OLAP) 客户端细节。
 *
 * <p>实现位于 {@code infra} 层；{@code app} 层经本端口编排自检，不直接耦合具体客户端。
 */
public interface OlapConnectivityPort {

    /**
     * 在当前租户隔离边界内探测取数基础设施连通性。
     *
     * @return 连通快照
     */
    OlapStatus probe();
}
