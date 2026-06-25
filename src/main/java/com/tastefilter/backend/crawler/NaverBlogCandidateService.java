package com.tastefilter.backend.crawler;

import com.tastefilter.backend.client.naver.NaverBlogSearchClient;
import com.tastefilter.backend.client.naver.dto.NaverBlogSearchItem;
import com.tastefilter.backend.client.naver.dto.NaverBlogSearchResponse;
import com.tastefilter.backend.dto.CrawlCandidateSearchResult;
import com.tastefilter.backend.dto.CrawledReview;
import com.tastefilter.backend.model.FoodCategory;
import com.tastefilter.backend.model.Region;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class NaverBlogCandidateService {

    private final NaverBlogSearchClient searchClient;
    private final SearchQueryFactory queryFactory;

    public NaverBlogCandidateService(
            NaverBlogSearchClient searchClient,
            SearchQueryFactory queryFactory
    ) {
        this.searchClient = searchClient;
        this.queryFactory = queryFactory;
    }

    // 검색 API 결과는 본문이 아니므로 저장하지 않고 상세 분석 대기 후보로만 변환한다.
    public CrawlCandidateSearchResult search(Region region, FoodCategory category) {
        String query = queryFactory.create(region, category);
        NaverBlogSearchResponse response = searchClient.searchLatest(query);

        if (response == null) {
            throw new IllegalStateException("Naver blog search returned an empty response");
        }

        List<NaverBlogSearchItem> items = response.items() == null
                ? List.of()
                : response.items();

        List<CrawledReview> candidates = items.stream()
                .filter(item -> item.link() != null && !item.link().isBlank())
                .map(this::toCandidate)
                .toList();

        return new CrawlCandidateSearchResult(query, response.total(), candidates);
    }

    private CrawledReview toCandidate(NaverBlogSearchItem item) {
        String title = cleanSearchText(item.title());
        String description = cleanSearchText(item.description());
        String searchableText = (title + " " + description).trim();

        return new CrawledReview(
                item.link(),
                searchableText,
                description,
                false,
                false,
                false
        );
    }

    private String cleanSearchText(String value) {
        if (value == null) {
            return "";
        }

        // 네이버 검색 API가 일치 키워드에 넣는 <b> 태그만 제거한 뒤 HTML 엔티티를 복원한다.
        String withoutHighlight = value
                .replace("<b>", "")
                .replace("</b>", "");

        return HtmlUtils.htmlUnescape(withoutHighlight).trim();
    }
}
