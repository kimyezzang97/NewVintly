package com.vintly.common.exception.memebr;

import com.vintly.common.exception.StatusEnum;
import lombok.Getter;

/**
 * 회원가입 - 닉네임, email 중복 확인
 */
@Getter
public class ConflictMemberException extends RuntimeException {

    private final StatusEnum status;
    // nickname or email 중복
    public ConflictMemberException(){
        this.status = StatusEnum.JOIN_CONFLICT;
    };
}
