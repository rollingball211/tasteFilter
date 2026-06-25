package com.tastefilter.backend.crawler;

import com.tastefilter.backend.dto.CrawlCandidateSearchResult;
import com.tastefilter.backend.dto.CrawlIngestionResult;
import com.tastefilter.backend.dto.CrawledReview;
import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.service.RestaurantService;
import com.tastefilter.backend.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class NaverBlogCrawlOrchestrator {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final NaverBlogCandidateService candidateService;
    private final NaverBlogPostParser postParser;

    public NaverBlogCrawlOrchestrator(
            RestaurantService restaurantService,
            ReviewService reviewService,
            NaverBlogCandidateService candidateService,
            NaverBlogPostParser postParser
    ) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.candidateService = candidateService;
        this.postParser = postParser;
    }

    // 스케줄러나 관리자 API가 호출할 수 있도록 한 식당 기준의 검색-파싱-필터-저장 흐름을 묶는다.
    public CrawlIngestionResult crawlForRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantService.getById(restaurantId);
        CrawlCandidateSearchResult searchResult = candidateService.search(
                restaurant.getRegion(),
                restaurant.getCategory()
        );

        int duplicateCount = 0;
        int parsedCount = 0;
        int savedCount = 0;
        int rejectedCount = 0;
        int failedCount = 0;

        for (CrawledReview candidate : searchResult.candidates()) {
            if (reviewService.existsByBlogUrl(candidate.blogUrl())) {
                duplicateCount++;
                continue;
            }

            try {
                CrawledReview parsedReview = postParser.parse(candidate);
                parsedCount++;

                if (reviewService.saveFiltered(restaurant, parsedReview).isPresent()) {
                    savedCount++;
                } else {
                    rejectedCount++;
                }
            } catch (BlogPostParseException e) {
                failedCount++;
            }
        }

        return new CrawlIngestionResult(
                searchResult.query(),
                searchResult.totalSearchResults(),
                searchResult.candidates().size(),
                duplicateCount,
                parsedCount,
                savedCount,
                rejectedCount,
                failedCount
        );
    }
}
