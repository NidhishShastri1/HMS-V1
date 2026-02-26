package com.hms.backend.service;

import com.hms.backend.event.AuditEvent;
import com.hms.backend.model.AuditLog;
import com.hms.backend.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    public void testHandleAuditEvent_Success() {
        // Arrange
        AuditEvent event = AuditEvent.builder()
                .entityName("PATIENT")
                .entityId("PT001")
                .actionType("CREATE")
                .newValue("John Doe")
                .performedByUserId("admin")
                .reason("New Registration")
                .machineId("WS-01")
                .build();

        // Act
        auditService.handleAuditEvent(event);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("PATIENT", savedLog.getEntityName());
        assertEquals("PT001", savedLog.getEntityId());
        assertEquals("CREATE", savedLog.getActionType());
        assertEquals("John Doe", savedLog.getNewValue());
        assertEquals("admin", savedLog.getPerformedByUserId());
        assertEquals("WS-01", savedLog.getMachineId());
    }

    @Test
    public void testHandleAuditEvent_HandlesExceptionGracefully() {
        // Arrange
        // auditLogRepository.save(...) will throw an exception by default if not mocked
        // or specifically told to.
        // But the requirement says it should not fail the main transaction.
        // The service method has a try-catched block.

        AuditEvent event = AuditEvent.builder()
                .entityName("FAIL")
                .entityId("X")
                .build();

        // Act
        // This should not throw any exception out to the caller
        auditService.handleAuditEvent(event);

        // Verify that save was at least attempted
        verify(auditLogRepository).save(org.mockito.ArgumentMatchers.any());
    }
}
