package com.hms.backend.service;

import com.hms.backend.event.AuditEvent;
import com.hms.backend.model.AuditLog;
import com.hms.backend.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Passive, non-blocking listener that records data ONLY after
     * the main transaction is committed successfully.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuditEvent(AuditEvent event) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .entityName(event.getEntityName())
                    .entityId(event.getEntityId())
                    .actionType(event.getActionType())
                    .oldValue(event.getOldValue())
                    .newValue(event.getNewValue())
                    .performedByUserId(event.getPerformedByUserId())
                    .reason(event.getReason())
                    .machineId(event.getMachineId())
                    .build();

            auditLogRepository.save(logEntry);
            log.info("Audit log saved for {}/{}", event.getEntityName(), event.getEntityId());
        } catch (Exception e) {
            // Requirement 6: If audit fails icon to log error but NOT fail billing
            // transaction
            log.error("CRITICAL: Failed to write audit log for entity {}: {}",
                    event.getEntityName(), e.getMessage());
        }
    }
}
