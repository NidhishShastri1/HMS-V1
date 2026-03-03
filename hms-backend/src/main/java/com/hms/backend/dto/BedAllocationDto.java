package com.hms.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class BedAllocationDto {
    private Long id;
    private String visitId;
    private String bedNumber;
    private String wardName;
    private Instant allocatedAt;
    private Instant releasedAt;
    private Long allocatedByUserId;
}
