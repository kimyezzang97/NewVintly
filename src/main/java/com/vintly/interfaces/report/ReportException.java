package com.vintly.interfaces.report;

public class ReportException {

    // 같은 대상을 두 번 신고
    public static class DuplicateReportException extends RuntimeException {
        public DuplicateReportException() {
            super("이미 신고한 대상입니다.");
        }
    }

    // 신고 대상이 존재하지 않음 (이미 삭제됐거나 잘못된 ID)
    public static class ReportTargetNotFoundException extends RuntimeException {
        public ReportTargetNotFoundException() {
            super("신고 대상을 찾을 수 없습니다.");
        }
    }

    // 본인이 작성한 콘텐츠 신고
    public static class SelfReportException extends RuntimeException {
        public SelfReportException() {
            super("본인이 작성한 콘텐츠는 신고할 수 없습니다.");
        }
    }
}
