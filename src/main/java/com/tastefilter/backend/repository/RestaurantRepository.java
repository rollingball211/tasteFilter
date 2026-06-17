package com.tastefilter.backend.repository;

import com.tastefilter.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByNaverPlaceId(String naverPlaceId);
    boolean existsByNaverPlaceId(String naverPlaceId);
}
