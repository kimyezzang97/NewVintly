package com.vintly.member.model.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// MEMBER - useYn(사용여부)
@Getter
@RequiredArgsConstructor
public enum Use {
    Y("사용"), // 사용
    N("탈퇴"), // 탈퇴
    X("추방"), // 추방
    K("대기"); // 대기

    private final String message;
}
