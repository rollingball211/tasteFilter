package com.tastefilter.backend.controller;

import com.tastefilter.backend.dto.RestaurantReviewsResponse;
import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.model.Review;
import com.tastefilter.backend.service.RestaurantService;
import com.tastefilter.backend.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    public RestaurantReviewController(
            RestaurantService restaurantService,
            ReviewService reviewService
    ) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    // 크롤링 완료 여부와 저장 결과를 확인하고, 이후 사용자 화면에서도 같은 응답을 재사용한다.
    @GetMapping("/{restaurantId}/reviews")
    public RestaurantReviewsResponse getReviews(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);

        Restaurant restaurant = restaurantService.getById(restaurantId);
        Page<Review> reviewPage = reviewService.getLatestReviews(restaurantId, page, size);

        return RestaurantReviewsResponse.from(restaurant, reviewPage);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be 0 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }
}
