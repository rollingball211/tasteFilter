package com.tastefilter.backend.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminApiExceptionHandler {

    // 모든 API에서 잘못된 식당 ID를 서버 장애와 구분할 수 있도록 404 응답으로 변환한다.
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
