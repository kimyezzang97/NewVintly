package com.vintly.common.exception.memebr;

import com.vintly.common.exception.StatusEnum;
import lombok.Getter;

/**
 * 로그인 - access 토큰 만료
 */
@Getter
public class AccessExpiredException extends RuntimeException {

    public AccessExpiredException(){

    };
}
