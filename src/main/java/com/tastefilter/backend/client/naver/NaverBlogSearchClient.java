package com.tastefilter.backend.client.naver;

import com.tastefilter.backend.client.naver.dto.NaverBlogSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NaverBlogSearchClient {

    private static final String BLOG_SEARCH_PATH = "/v1/search/blog.json";

    private final NaverApiProperties properties;
    private final RestClient restClient;

    public NaverBlogSearchClient(NaverApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    // 정기 수집은 최신 게시글부터 확인하므로 날짜순 첫 페이지를 기본 검색으로 제공한다.
    public NaverBlogSearchResponse searchLatest(String query) {
        return search(query, 100, 1, NaverBlogSort.DATE);
    }

    public NaverBlogSearchResponse search(
            String query,
            int display,
            int start,
            NaverBlogSort sort
    ) {
        validateRequest(query, display, start, sort);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BLOG_SEARCH_PATH)
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", sort.getValue())
                        .build())
                .header("X-Naver-Client-Id", properties.getClientId())
                .header("X-Naver-Client-Secret", properties.getClientSecret())
                .retrieve()
                .body(NaverBlogSearchResponse.class);
    }

    private void validateRequest(
            String query,
            int display,
            int start,
            NaverBlogSort sort
    ) {
        if (!properties.hasCredentials()) {
            throw new IllegalStateException(
                    "NAVER_CLIENT_ID and NAVER_CLIENT_SECRET must be configured"
            );
        }
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        if (display < 1 || display > 100) {
            throw new IllegalArgumentException("display must be between 1 and 100");
        }
        if (start < 1 || start > 1000) {
            throw new IllegalArgumentException("start must be between 1 and 1000");
        }
        if (sort == null) {
            throw new IllegalArgumentException("sort must not be null");
        }
    }
}
