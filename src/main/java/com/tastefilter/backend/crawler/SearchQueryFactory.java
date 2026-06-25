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

    // Enum을 검색용 한글 키워드로 변환해 검색어 생성 규칙이 여러 클래스에 흩어지지 않게 한다.
    public String create(Region region, FoodCategory category) {
        if (region == null) {
            throw new IllegalArgumentException("region must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category must not be null");
        }

        return "%s %s 맛집".formatted(
                REGION_KEYWORDS.get(region),
                CATEGORY_KEYWORDS.get(category)
        );
    }
}
