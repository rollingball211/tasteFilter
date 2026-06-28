package com.tastefilter.backend.dto;

import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.model.Review;
import org.springframework.data.domain.Page;

import java.util.List;

public record RestaurantReviewsResponse(
        Long restaurantId,
        String restaurantName,
        long totalElements,
        int totalPages,
        int page,
        int size,
        List<ReviewResponse> reviews
) {

    public static RestaurantReviewsResponse from(Restaurant restaurant, Page<Review> reviewPage) {
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::from)
                .toList();

        return new RestaurantReviewsResponse(
                restaurant.getId(),
                restaurant.getName(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviews
        );
    }
}
