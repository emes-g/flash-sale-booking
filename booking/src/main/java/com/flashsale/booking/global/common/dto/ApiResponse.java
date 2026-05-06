package com.flashsale.booking.global.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "요청 성공");
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
