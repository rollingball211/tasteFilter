package com.tastefilter.backend.service;

import com.tastefilter.backend.dto.CrawledReview;
import com.tastefilter.backend.dto.ReviewDecision;
import com.tastefilter.backend.dto.ReviewFilterResult;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ReviewFilterService {

    private static final int BASE_SCORE = 70;
    private static final int RECEIPT_BONUS = 20;
    private static final int ADVERTISEMENT_PENALTY = 60;
    private static final int EVENT_PENALTY = 25;
    private static final int ACCEPTANCE_SCORE = 50;

    private static final Pattern ADVERTISEMENT_PATTERN = Pattern.compile(
            "협찬|광고|체험단|원고료|제품을\\s*제공받|서비스를\\s*제공받|소정의\\s*(?:대가|지원)"
    );

    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "리뷰\\s*이벤트|방문\\s*이벤트|이벤트에\\s*참여|후기\\s*작성\\s*시|리뷰\\s*작성\\s*시"
    );

    public ReviewFilterResult evaluate(CrawledReview review) {
        boolean advertisementSuspected = review.hasDisclosureBanner()
                || ADVERTISEMENT_PATTERN.matcher(review.content()).find();
        boolean eventSuspected = EVENT_PATTERN.matcher(review.content()).find();

        int trustScore = BASE_SCORE;
        if (review.hasReceiptAuth()) {
            trustScore += RECEIPT_BONUS;
        }
        if (advertisementSuspected) {
            trustScore -= ADVERTISEMENT_PENALTY;
        }
        if (eventSuspected) {
            trustScore -= EVENT_PENALTY;
        }

        trustScore = Math.max(0, Math.min(100, trustScore));

        ReviewDecision decision = decide(review, advertisementSuspected, trustScore);

        return new ReviewFilterResult(
                trustScore,
                advertisementSuspected,
                eventSuspected,
                decision
        );
    }

    private ReviewDecision decide(
            CrawledReview review,
            boolean advertisementSuspected,
            int trustScore
    ) {
        // 검색 요약에서도 명확한 광고 신호가 발견되면 상세 분석 전이라도 제외한다.
        if (advertisementSuspected || trustScore < ACCEPTANCE_SCORE) {
            return ReviewDecision.REJECTED;
        }

        // 검색 API 요약만으로 광고가 아니라고 확정하지 않고 상세 본문 분석을 기다린다.
        if (!review.detailAnalyzed()) {
            return ReviewDecision.NEEDS_ANALYSIS;
        }

        return ReviewDecision.ACCEPTED;
    }
}
