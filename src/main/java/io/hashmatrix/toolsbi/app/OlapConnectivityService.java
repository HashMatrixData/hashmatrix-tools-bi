package io.hashmatrix.toolsbi.app;

import io.hashmatrix.starter.audit.AuditEvent;
import io.hashmatrix.starter.audit.AuditRecorder;
import io.hashmatrix.toolsbi.domain.OlapStatus;
import io.hashmatrix.toolsbi.domain.port.OlapConnectivityPort;
import org.springframework.stereotype.Service;

/**
 * 取数连通自检应用服务：在当前租户上下文内探测 Doris，并复用 {@code starter-audit} 记审计。
 *
 * <p>审计事件由 {@link AuditEvent#of} 自动加盖当前租户（{@code starter-tenant}），跨租户绝不串。
 */
@Service
public class OlapConnectivityService {

    private final OlapConnectivityPort connectivity;
    private final AuditRecorder auditRecorder;

    public OlapConnectivityService(OlapConnectivityPort connectivity, AuditRecorder auditRecorder) {
        this.connectivity = connectivity;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 探测取数基础设施连通并记录审计。
     *
     * @return 连通快照
     */
    public OlapStatus probe() {
        OlapStatus status = connectivity.probe();
        auditRecorder.record(
                AuditEvent.of(
                        "system",
                        "OLAP_PROBE",
                        status.tenantCatalog(),
                        status.healthy() ? AuditEvent.Outcome.SUCCESS : AuditEvent.Outcome.FAILURE,
                        null));
        return status;
    }
}
