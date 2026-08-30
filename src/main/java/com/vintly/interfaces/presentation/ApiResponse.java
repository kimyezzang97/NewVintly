package com.vintly.interfaces.presentation;

// [record] private final, equals(), toString(), hashCode(), getter 자동 생성
public record ApiResponse<T>(boolean success, int code, String msg, T data) {

}
