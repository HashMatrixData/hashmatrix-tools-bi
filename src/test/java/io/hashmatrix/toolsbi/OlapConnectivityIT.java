package io.hashmatrix.toolsbi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hashmatrix.test.fixtures.MockTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 集成切片：用 Testcontainers 起 MySQL 作 Doris 的「MySQL 协议替身」，验证工程基座经真实 JDBC 的
 * 取数连通、每租户取数库名派生与 actuator 健康——即本仓验收「mvn verify → 取数自检绿」。
 *
 * <p>用替身而非真实 Doris：Doris 镜像重、起得慢、对 CI 资源要求高（DoD 明确允许「Doris 或以 mock 替身」）；
 * 本仓适配只做 MySQL 协议层连通探针，替身足以守护接线正确性。替身只能守护「JDBC 连通 + 协议握手 +
 * 接线装配 + 租户库名派生」，**守护不了真实的库级隔离取数路由**——基座阶段适配器尚不 {@code USE}
 * 可能未开通的租户库（见 {@code DorisConnectivityAdapter}）；真正的「按租户库隔离取数」随报表/分析
 * 业务查询落地时补集成守护。Doris SQL 方言/OLAP 行为由 {@code docker-compose.local.yml} 的真实
 * Doris 覆盖。走 failsafe（{@code *IT}），{@code mvn package} 不触发。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OlapConnectivityIT {

    @Container
    static final MySQLContainer<?> DORIS_STANDIN =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("information_schema_demo")
                    .withUsername("bi")
                    .withPassword("bi");

    @DynamicPropertySource
    static void olapProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> DORIS_STANDIN.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", DORIS_STANDIN::getUsername);
        registry.add("spring.datasource.password", DORIS_STANDIN::getPassword);
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void probeReportsOlapReachableUnderTenantCatalog() throws Exception {
        mvc.perform(get("/api/tools-bi/probe").header("X-Tenant-Id", MockTenants.ACME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.tenantCatalog").value("bi_acme"))
                .andExpect(jsonPath("$.data.doris").value(true));
    }

    @Test
    void derivesPerTenantCatalogThroughHttp() throws Exception {
        // 连续探测两个租户：验证经真实 JDBC 连通时，各租户解析到各自取数库名（路由目标，非跨租户串）。
        // 注意：基座阶段适配器未真正 USE 租户库，故此处守护的是「按租户派生取数库名」，
        // 真实库级隔离取数路由随业务查询落地补守护（见类注释）。
        mvc.perform(get("/api/tools-bi/probe").header("X-Tenant-Id", MockTenants.ACME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantCatalog").value("bi_acme"))
                .andExpect(jsonPath("$.data.doris").value(true));
        mvc.perform(get("/api/tools-bi/probe").header("X-Tenant-Id", MockTenants.TENANT_DEMO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantCatalog").value("bi_tenant_demo"))
                .andExpect(jsonPath("$.data.doris").value(true));
    }

    @Test
    void actuatorHealthIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
