package com.vintly.common.exception.memebr;

import lombok.Getter;

@Getter
public class NicknameValidException extends RuntimeException{

    // 닉네임 규칙 valid
    public NicknameValidException() {}
}
