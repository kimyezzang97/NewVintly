package com.vintly.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// REPORT - reason(신고 사유)
@Getter
@RequiredArgsConstructor
public enum ReportReason {
    OBSCENE("음란물"),
    ABUSE("욕설·비방"),
    SPAM("광고·스팸"),
    FLOOD("도배"),
    ETC("기타");

    private final String description;
}
