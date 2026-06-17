package com.tastefilter.backend.repository;

import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantOrderByCrawledAtDesc(Restaurant restaurant);

    long countByRestaurant(Restaurant restaurant);
}
