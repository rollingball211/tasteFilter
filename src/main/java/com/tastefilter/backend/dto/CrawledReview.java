package com.tastefilter.backend.dto;

public record CrawledReview(
        String blogUrl,
        String content,
        String contentSnippet,
        boolean hasReceiptAuth,
        boolean hasDisclosureBanner,
        boolean detailAnalyzed
) {

    public CrawledReview {
        if (blogUrl == null || blogUrl.isBlank()) {
            throw new IllegalArgumentException("blogUrl must not be blank");
        }

        // 크롤링 결과에 본문이 없더라도 필터가 null 처리에 의존하지 않게 빈 문자열로 정규화한다.
        content = content == null ? "" : content;
        contentSnippet = contentSnippet == null ? "" : contentSnippet;
    }
}
