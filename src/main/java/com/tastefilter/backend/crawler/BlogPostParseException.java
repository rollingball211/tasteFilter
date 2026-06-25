package com.tastefilter.backend.crawler;

public class BlogPostParseException extends RuntimeException {

    public BlogPostParseException(String message) {
        super(message);
    }

    public BlogPostParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
