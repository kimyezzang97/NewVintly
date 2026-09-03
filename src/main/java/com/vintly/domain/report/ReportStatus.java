package com.vintly.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// REPORT - status(신고 처리 상태)
// 접수 단계에서는 PENDING만 저장된다. 상태 전이는 관리자 검토 기능에서 다룬다.
@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("접수"),
    ACCEPTED("처리 완료"),
    REJECTED("기각");

    private final String description;
}
