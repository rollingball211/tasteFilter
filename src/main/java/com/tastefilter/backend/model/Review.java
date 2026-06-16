package com.tastefilter.backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실무 필수 설정: 불필요한 조회를 막기 위해 반드시 지연 로딩(LAZY)을 사용합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 1000)
    private String blogUrl;

    @Column(columnDefinition = "TEXT")
    private String contentSnippet;

    @Column(nullable = false)
    private Integer trustScore;

    private boolean hasReceiptAuth;

    private boolean isEventSuspected;

    @Column(name = "crawled_at", updatable = false)
    private LocalDateTime crawledAt;

    @PrePersist
    protected void onCreate() {
        this.crawledAt = LocalDateTime.now();
    }
}
