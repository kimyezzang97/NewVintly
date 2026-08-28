package com.vintly.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// MEMBER - useStatus(계정 이용 상태)
@Getter
@RequiredArgsConstructor
public enum Use {
    Y("사용"),
    X("추방"),
    K("대기"),
    E("탈퇴");

    private final String message;
}
