package com.tastefilter.backend.client.naver;

public enum NaverBlogSort {
    SIMILARITY("sim"),
    DATE("date");

    private final String value;

    NaverBlogSort(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
