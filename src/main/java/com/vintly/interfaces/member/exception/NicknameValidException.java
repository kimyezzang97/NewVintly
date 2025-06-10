package com.vintly.interfaces.member.exception;

import lombok.Getter;

/**
 * 닉네임, 이메일 규칙 valid error
 */
@Getter
public class NicknameValidException extends RuntimeException{

    private final StatusEnum status;

    public NicknameValidException() {
        this.status = StatusEnum.VALID_ERROR;
    }
}
