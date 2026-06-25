package com.tastefilter.backend.dto;

import java.util.List;

public record CrawlCandidateSearchResult(
        String query,
        int totalSearchResults,
        List<CrawledReview> candidates
) {

    public CrawlCandidateSearchResult {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
