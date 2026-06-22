package com.tastefilter.backend.client.naver.dto;

import java.util.List;

public record NaverBlogSearchResponse(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<NaverBlogSearchItem> items
) {
}
