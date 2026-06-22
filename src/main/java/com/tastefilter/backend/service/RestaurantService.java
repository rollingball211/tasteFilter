package com.tastefilter.backend.service;

import com.tastefilter.backend.model.FoodCategory;
import com.tastefilter.backend.model.Region;
import com.tastefilter.backend.model.Restaurant;
import com.tastefilter.backend.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    // 크롤링할 때 같은 네이버 플레이스가 반복 수집되므로 기존 식당을 우선 재사용한다.
    @Transactional
    public Restaurant findOrCreate(
            String name,
            Region region,
            FoodCategory category,
            String naverPlaceId,
            Integer instaHashtagCount,
            Integer viralIndex
    ) {
        return restaurantRepository.findByNaverPlaceId(naverPlaceId)
                .orElseGet(() -> restaurantRepository.save(Restaurant.create(
                        name,
                        region,
                        category,
                        naverPlaceId,
                        instaHashtagCount,
                        viralIndex
                )));
    }

    // 식당 조회 실패 처리를 서비스 계층에 모아 호출자가 null을 직접 다루지 않게 한다.
    public Restaurant getById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Restaurant not found: " + restaurantId
                ));
    }
}
