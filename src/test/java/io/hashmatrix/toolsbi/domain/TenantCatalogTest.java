package io.hashmatrix.toolsbi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.hashmatrix.test.fixtures.MockTenants;
import org.junit.jupiter.api.Test;

class TenantCatalogTest {

    @Test
    void derivesPrefixedCatalogFromTenant() {
        assertThat(TenantCatalog.forTenant(MockTenants.ACME).name()).isEqualTo("bi_acme");
        assertThat(TenantCatalog.forTenant(MockTenants.TENANT_DEMO).name()).isEqualTo("bi_tenant_demo");
    }

    @Test
    void sanitizesUnsafeCharactersToUnderscore() {
        // 大小写归一 + 非 [a-z0-9_] 折叠，确保可安全用于库名限定（杜绝注入）
        assertThat(TenantCatalog.forTenant("Acme Corp").name()).isEqualTo("bi_acme_corp");
        assertThat(TenantCatalog.forTenant("t1; DROP").name()).isEqualTo("bi_t1__drop");
    }

    @Test
    void rejectsBlankTenant() {
        assertThatThrownBy(() -> TenantCatalog.forTenant("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
