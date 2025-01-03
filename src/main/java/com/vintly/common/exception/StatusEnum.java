package com.vintly.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // 필수(final)변수 파라미터로 가지는 생성자 생성
@Getter
public enum StatusEnum {
    OK("OK"),

    // validation
    BAD_REQUEST(""),

    // Member
    JOIN_CONFLICT("중복 확인을 해주세요."), // 회원가입시 계정 중복일 경우
    VALID_ERROR("규칙을 지켜주세요."), //
    MEMBER_NOT_EXIST("계정이 존재하지 않습니다."); //

    private final String message;
}
