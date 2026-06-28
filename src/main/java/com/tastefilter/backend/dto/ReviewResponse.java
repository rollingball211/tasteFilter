package com.tastefilter.backend.dto;

import com.tastefilter.backend.model.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String blogUrl,
        String contentSnippet,
        int trustScore,
        boolean hasReceiptAuth,
        boolean eventSuspected,
        LocalDateTime crawledAt
) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBlogUrl(),
                review.getContentSnippet(),
                review.getTrustScore(),
                review.isHasReceiptAuth(),
                review.isEventSuspected(),
                review.getCrawledAt()
        );
    }
}
