package com.vintly.interfaces.report;

public class ReportException {

    // 같은 대상을 두 번 신고
    public static class DuplicateReportException extends RuntimeException {
        public DuplicateReportException() {
            super("이미 신고한 대상입니다.");
        }
    }
}
