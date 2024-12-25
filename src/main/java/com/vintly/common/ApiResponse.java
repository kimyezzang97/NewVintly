package com.vintly.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private HttpStatus status;
    private String msg;
    private T data;

    @Builder
    public ApiResponse(HttpStatus status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
}
