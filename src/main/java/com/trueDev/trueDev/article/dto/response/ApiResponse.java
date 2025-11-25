package com.trueDev.trueDev.article.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "응답 래퍼")
public record ApiResponse<T>(String message, T data) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(message, data);
    }
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(message, null);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }
}