package com.tastefilter.backend.crawler;

import com.tastefilter.backend.model.FoodCategory;
import com.tastefilter.backend.model.Region;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SearchQueryFactory {

    private static final Map<Region, String> REGION_KEYWORDS = Map.of(
            Region.SEONGSU, "성수",
            Region.GANGNAM, "강남",
            Region.HONGDAE, "홍대",
            Region.JONGNO, "종로",
            Region.KONDAE, "건대"
    );

    private static final Map<FoodCategory, String> CATEGORY_KEYWORDS = Map.of(
            FoodCategory.KOREAN, "한식",
            FoodCategory.JAPANESE, "일식",
            FoodCategory.CHINESE, "중식",
            FoodCategory.WESTERN, "양식",
            FoodCategory.ASIAN, "아시아 음식",
            FoodCategory.ETC, "음식"
    );

    // 식당명을 포함해 같은 지역과 카테고리의 다른 식당 리뷰가 섞일 가능성을 줄인다.
    public String create(String restaurantName, Region region, FoodCategory category) {
        if (restaurantName == null || restaurantName.isBlank()) {
            throw new IllegalArgumentException("restaurantName must not be blank");
        }
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }

        return "%s %s %s 맛집".formatted(
                REGION_KEYWORDS.get(region),
                restaurantName.trim(),
                CATEGORY_KEYWORDS.get(category)
        );
    }
}
