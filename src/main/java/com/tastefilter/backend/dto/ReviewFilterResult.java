package com.tastefilter.backend.dto;

public record ReviewFilterResult(
        int trustScore,
        boolean advertisementSuspected,
        boolean eventSuspected,
        ReviewDecision decision
) {
}
