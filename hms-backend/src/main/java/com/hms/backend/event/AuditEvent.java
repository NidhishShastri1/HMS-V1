package com.hms.backend.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditEvent {
    private String entityName;
    private String entityId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private String performedByUserId;
    private String reason;
    private String machineId;
}
