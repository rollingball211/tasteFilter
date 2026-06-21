package com.tastefilter.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING) // DB에 숫자가 아닌 'SEONGSU' 문자열 그대로 저장되도록 설정
    @Column(nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodCategory category;

    @Column(name = "naver_place_id", unique = true)
    private String naverPlaceId;

    @Column(name = "insta_hashtag_count")
    private Integer instaHashtagCount;

    @Column(name = "viral_index")
    private Integer viralIndex;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private Restaurant(
            String name,
            Region region,
            FoodCategory category,
            String naverPlaceId,
            Integer instaHashtagCount,
            Integer viralIndex
    ) {
        this.name = name;
        this.region = region;
        this.category = category;
        this.naverPlaceId = naverPlaceId;
        this.instaHashtagCount = instaHashtagCount;
        this.viralIndex = viralIndex;
    }

    public static Restaurant create(
            String name,
            Region region,
            FoodCategory category,
            String naverPlaceId,
            Integer instaHashtagCount,
            Integer viralIndex
    ) {
        return new Restaurant(name, region, category, naverPlaceId, instaHashtagCount, viralIndex);
    }

    // Entity가 처음 DB에 저장될 때 자동으로 현재 시간을 세팅합니다.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
