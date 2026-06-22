package com.tastefilter.backend.client.naver.dto;

public record NaverBlogSearchItem(
        String title,
        String link,
        String description,
        String bloggername,
        String bloggerlink,
        String postdate
) {
}
