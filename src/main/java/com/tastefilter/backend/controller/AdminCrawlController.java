package com.tastefilter.backend.controller;

import com.tastefilter.backend.crawler.NaverBlogCrawlOrchestrator;
import com.tastefilter.backend.dto.CrawlIngestionResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/crawls")
public class AdminCrawlController {

    private final NaverBlogCrawlOrchestrator crawlOrchestrator;

    public AdminCrawlController(NaverBlogCrawlOrchestrator crawlOrchestrator) {
        this.crawlOrchestrator = crawlOrchestrator;
    }

    // 스케줄러를 붙이기 전에 식당 한 건의 전체 수집 흐름을 수동으로 검증할 수 있게 한다.
    @PostMapping("/restaurants/{restaurantId}")
    public CrawlIngestionResult crawlRestaurant(
            @PathVariable("restaurantId") Long restaurantId
    ) {
        return crawlOrchestrator.crawlForRestaurant(restaurantId);
    }
}
