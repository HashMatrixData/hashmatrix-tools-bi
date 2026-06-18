package io.hashmatrix.toolsbi.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hashmatrix.test.fixtures.MockTenants;
import io.hashmatrix.toolsbi.app.OlapConnectivityService;
import io.hashmatrix.toolsbi.domain.OlapStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web 层装配冒烟（切片，无需 Docker，随 surefire/{@code mvn package} 运行）：
 * 守护 {@link ProbeController} 路由 + {@code starter-web} 统一返回（{@code ApiResponse}）契约装配——
 * 把「装配是否成立」前移到无外部容器的打包路径，与「只 clone 即可 mvn package」DoD 自洽。
 *
 * <p>取数连通（真实 JDBC）与 actuator 健康由 {@code OlapConnectivityIT}（failsafe/需 Docker）守护。
 */
@WebMvcTest(ProbeController.class)
class ProbeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OlapConnectivityService connectivityService;

    @Test
    void probeReturnsUnifiedResponseWithProbeData() throws Exception {
        when(connectivityService.probe()).thenReturn(new OlapStatus("bi_acme", true));

        mvc.perform(get("/api/tools-bi/probe").header("X-Tenant-Id", MockTenants.ACME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.tenantCatalog").value("bi_acme"))
                .andExpect(jsonPath("$.data.doris").value(true));
    }
}
