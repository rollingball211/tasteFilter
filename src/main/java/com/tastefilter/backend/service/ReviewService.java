package com.tastefilter.backend.service;

import com.tastefilter.backend.dto.CrawledReview;
import com.tastefilter.backend.dto.ReviewDecision;
import com.tastefilter.backend.dto.ReviewFilterResult;
import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.model.Review;
import com.tastefilter.backend.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewFilterService reviewFilterService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewFilterService reviewFilterService
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewFilterService = reviewFilterService;
    }

    // 광고 필터를 통과한 크롤링 결과만 저장 흐름으로 전달한다.
    @Transactional
    public Optional<Review> saveFiltered(Restaurant restaurant, CrawledReview crawledReview) {
        ReviewFilterResult filterResult = reviewFilterService.evaluate(crawledReview);
        if (filterResult.decision() != ReviewDecision.ACCEPTED) {
            return Optional.empty();
        }

        return saveIfNotExists(
                restaurant,
                crawledReview.blogUrl(),
                crawledReview.contentSnippet(),
                filterResult.trustScore(),
                crawledReview.hasReceiptAuth(),
                filterResult.eventSuspected()
        );
    }

    // 상세 페이지를 가져오기 전에 이미 저장된 URL인지 확인해 불필요한 네트워크 요청을 줄인다.
    public boolean existsByBlogUrl(String blogUrl) {
        return reviewRepository.existsByBlogUrl(blogUrl);
    }

    // 동일한 블로그 URL의 재수집을 건너뛰고, 저장 여부를 Optional로 명확하게 전달한다.
    @Transactional
    public Optional<Review> saveIfNotExists(
            Restaurant restaurant,
            String blogUrl,
            String contentSnippet,
            Integer trustScore,
            boolean hasReceiptAuth,
            boolean isEventSuspected
    ) {
        if (reviewRepository.existsByBlogUrl(blogUrl)) {
            return Optional.empty();
        }

        Review review = Review.create(
                restaurant,
                blogUrl,
                contentSnippet,
                trustScore,
                hasReceiptAuth,
                isEventSuspected
        );

        return Optional.of(reviewRepository.save(review));
    }

    // API에서 최신 리뷰를 바로 제공할 수 있도록 크롤링 시각 내림차순 조회를 사용한다.
    public List<Review> getLatestReviews(Restaurant restaurant) {
        return reviewRepository.findByRestaurantOrderByCrawledAtDesc(restaurant);
    }

    // 조회 API가 리뷰 전체를 메모리에 올리지 않도록 DB 단계에서 최신순 페이지를 잘라 가져온다.
    public Page<Review> getLatestReviews(Long restaurantId, int page, int size) {
        return reviewRepository.findByRestaurantIdOrderByCrawledAtDesc(
                restaurantId,
                PageRequest.of(page, size)
        );
    }

    // 리뷰 수 집계를 DB에 맡겨 전체 리뷰 목록을 메모리에 불러오지 않게 한다.
    public long countReviews(Restaurant restaurant) {
        return reviewRepository.countByRestaurant(restaurant);
    }
}
