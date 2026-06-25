package com.tastefilter.backend.crawler;

import com.tastefilter.backend.dto.CrawledReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class NaverBlogPostParser {

    private static final String USER_AGENT =
            "TasteFilterBot/0.1 (+http://localhost:8080)";
    private static final int TIMEOUT_MILLIS = 10_000;
    private static final int MAX_BODY_SIZE_BYTES = 5 * 1024 * 1024;
    private static final int SNIPPET_LENGTH = 500;

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "blog.naver.com",
            "m.blog.naver.com"
    );

    private static final Pattern DISCLOSURE_IMAGE_PATTERN = Pattern.compile(
            "공정위|협찬|광고|체험단|원고료|제공받",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RECEIPT_IMAGE_PATTERN = Pattern.compile(
            "영수증|receipt",
            Pattern.CASE_INSENSITIVE
    );

    // 네이버 검색 요약 후보를 실제 게시글 본문으로 보강한 뒤에만 상세 분석 완료로 표시한다.
    public CrawledReview parse(CrawledReview candidate) {
        URI blogUri = validateNaverBlogUri(candidate.blogUrl());

        try {
            Document wrapperDocument = fetch(blogUri);
            Document contentDocument = resolveContentDocument(blogUri, wrapperDocument);
            Element contentRoot = findContentRoot(contentDocument);
            String content = normalizeText(contentRoot.text());

            if (content.isBlank()) {
                throw new BlogPostParseException(
                        "Naver blog content is empty: " + candidate.blogUrl()
                );
            }

            boolean hasDisclosureBanner = candidate.hasDisclosureBanner()
                    || containsImageSignal(contentRoot, DISCLOSURE_IMAGE_PATTERN);
            boolean hasReceiptAuth = candidate.hasReceiptAuth()
                    || containsImageSignal(contentRoot, RECEIPT_IMAGE_PATTERN);

            return new CrawledReview(
                    candidate.blogUrl(),
                    content,
                    createSnippet(content),
                    hasReceiptAuth,
                    hasDisclosureBanner,
                    true
            );
        } catch (IOException e) {
            throw new BlogPostParseException(
                    "Failed to fetch Naver blog post: " + candidate.blogUrl(),
                    e
            );
        }
    }

    private Document fetch(URI uri) throws IOException {
        Document document = Jsoup.connect(uri.toString())
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .maxBodySize(MAX_BODY_SIZE_BYTES)
                .followRedirects(true)
                .get();

        // 리다이렉트 후 외부 도메인으로 벗어나는 요청도 허용하지 않는다.
        validateNaverBlogUri(document.location());
        return document;
    }

    private Document resolveContentDocument(URI blogUri, Document wrapperDocument)
            throws IOException {
        if (findContentRootOrNull(wrapperDocument) != null) {
            return wrapperDocument;
        }

        Element mainFrame = wrapperDocument.selectFirst("iframe#mainFrame");
        if (mainFrame == null || mainFrame.attr("src").isBlank()) {
            throw new BlogPostParseException(
                    "Naver blog mainFrame was not found: " + blogUri
            );
        }

        URI frameUri = blogUri.resolve(mainFrame.attr("src"));
        validateNaverBlogUri(frameUri.toString());
        return fetch(frameUri);
    }

    private Element findContentRoot(Document document) {
        Element contentRoot = findContentRootOrNull(document);
        if (contentRoot == null) {
            throw new BlogPostParseException("Naver blog content root was not found");
        }
        return contentRoot;
    }

    private Element findContentRootOrNull(Document document) {
        Element smartEditorRoot = document.selectFirst("div.se-main-container");
        if (smartEditorRoot != null) {
            return smartEditorRoot;
        }
        return document.selectFirst("#postViewArea");
    }

    private boolean containsImageSignal(Element contentRoot, Pattern pattern) {
        return contentRoot.select("img").stream()
                .map(this::imageMetadata)
                .anyMatch(value -> pattern.matcher(value).find());
    }

    private String imageMetadata(Element image) {
        return String.join(
                " ",
                image.attr("alt"),
                image.attr("title"),
                image.attr("src"),
                image.attr("data-lazy-src"),
                image.attr("data-linkdata")
        );
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String createSnippet(String content) {
        int codePointCount = content.codePointCount(0, content.length());
        if (codePointCount <= SNIPPET_LENGTH) {
            return content;
        }
        int endIndex = content.offsetByCodePoints(0, SNIPPET_LENGTH);
        return content.substring(0, endIndex);
    }

    private URI validateNaverBlogUri(String value) {
        try {
            URI uri = new URI(value);
            String host = uri.getHost();
            boolean validScheme = "https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme());

            if (!validScheme || host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
                throw new BlogPostParseException(
                        "Only Naver blog URLs are allowed: " + value
                );
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new BlogPostParseException("Invalid blog URL: " + value, e);
        }
    }
}
