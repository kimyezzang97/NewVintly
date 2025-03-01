package com.vintly.common.config;

import com.vintly.common.exception.StatusEnum;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private StatusEnum status;
    private String msg;
    private T data;

    @Builder
    public ApiResponse(StatusEnum status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
}
