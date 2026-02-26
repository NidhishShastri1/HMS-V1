package com.hms.backend.service;

import com.hms.backend.model.SecurityEvent;
import com.hms.backend.repository.SecurityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecurityService {

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Async
    public void logIntegrityViolation(String entityName, String entityId, String details) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                    .eventType("INTEGRITY_VIOLATION")
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .severity("CRITICAL")
                    .build();

            securityEventRepository.save(event);
            log.warn("INTEGRITY VIOLATION DETECTED: {}/{}", entityName, entityId);
        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage());
        }
    }
}
