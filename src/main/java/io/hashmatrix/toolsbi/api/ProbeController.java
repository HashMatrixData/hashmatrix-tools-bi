package io.hashmatrix.toolsbi.api;

import io.hashmatrix.starter.web.ApiResponse;
import io.hashmatrix.toolsbi.app.OlapConnectivityService;
import io.hashmatrix.toolsbi.domain.OlapStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 取数基础设施自检 API：演示分层骨架与公共能力复用——
 * starter-tenant 透传租户 → starter-web 统一返回 → app/infra 探测 Doris → starter-audit 记审计。
 *
 * <p>报表/自助分析/可视编排业务 API 在本基座之上落地，本基座只暴露取数连通自检。
 */
@RestController
@RequestMapping("/api/tools-bi")
public class ProbeController {

    private final OlapConnectivityService connectivityService;

    public ProbeController(OlapConnectivityService connectivityService) {
        this.connectivityService = connectivityService;
    }

    /** 在当前租户隔离边界内自检 Doris 取数连通。 */
    @GetMapping("/probe")
    public ApiResponse<OlapStatus> probe() {
        return ApiResponse.ok(connectivityService.probe());
    }
}
