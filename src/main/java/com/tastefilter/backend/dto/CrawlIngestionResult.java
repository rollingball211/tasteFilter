package com.tastefilter.backend.dto;

public record CrawlIngestionResult(
        String query,
        int totalSearchResults,
        int candidateCount,
        int duplicateCount,
        int parsedCount,
        int savedCount,
        int rejectedCount,
        int failedCount
) {
}
