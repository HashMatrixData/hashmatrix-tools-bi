package io.hashmatrix.toolsbi.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hashmatrix.starter.audit.AuditEvent;
import io.hashmatrix.starter.audit.AuditRecorder;
import io.hashmatrix.toolsbi.domain.OlapStatus;
import io.hashmatrix.toolsbi.domain.port.OlapConnectivityPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OlapConnectivityServiceTest {

    @Mock private OlapConnectivityPort connectivity;
    @Mock private AuditRecorder auditRecorder;

    @Test
    void returnsProbeAndAuditsSuccessWhenHealthy() {
        when(connectivity.probe()).thenReturn(new OlapStatus("bi_acme", true));
        OlapConnectivityService service = new OlapConnectivityService(connectivity, auditRecorder);

        OlapStatus status = service.probe();

        assertThat(status.healthy()).isTrue();
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo("OLAP_PROBE");
        assertThat(captor.getValue().target()).isEqualTo("bi_acme");
        assertThat(captor.getValue().outcome()).isEqualTo(AuditEvent.Outcome.SUCCESS);
    }

    @Test
    void auditsFailureWhenOlapDown() {
        when(connectivity.probe()).thenReturn(new OlapStatus("bi_acme", false));
        OlapConnectivityService service = new OlapConnectivityService(connectivity, auditRecorder);

        service.probe();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().outcome()).isEqualTo(AuditEvent.Outcome.FAILURE);
    }

    @Test
    void recordsExactlyOneAuditPerProbe() {
        when(connectivity.probe()).thenReturn(new OlapStatus("bi_acme", true));
        OlapConnectivityService service = new OlapConnectivityService(connectivity, auditRecorder);

        service.probe();

        verify(auditRecorder).record(any(AuditEvent.class));
    }
}
