package com.vintly.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// REPORT - targetType(신고 대상 종류)
// 대상별로 테이블을 나누지 않고 이 값 + targetId 조합으로 대상을 가리킨다.
@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    BOARD("게시글"),
    BOARD_COMMENT("게시판 댓글"),
    VINTAGE_COMMENT("매장 댓글");

    private final String description;
}
