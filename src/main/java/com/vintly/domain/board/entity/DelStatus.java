package com.vintly.domain.board.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DelStatus {
    N("정상"),
    W("작성자 삭제"),
    S("관리자 삭제");

    private final String description;
}
