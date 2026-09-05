package com.vintly.domain.report.dto;

import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.entity.Report;

import java.time.LocalDateTime;

public class ReportInfo {

    // 내 신고 내역 한 건. 신고자는 자기 자신이므로 담지 않는다.
    public record My(
            Long reportId,
            ReportTargetType targetType,
            Long targetId,
            ReportReason reason,
            String detail,
            ReportStatus status,
            LocalDateTime createdAt
    ) {
        public static My from(Report report) {
            return new My(
                    report.getReportId(),
                    report.getTargetType(),
                    report.getTargetId(),
                    report.getReason(),
                    report.getDetail(),
                    report.getStatus(),
                    report.getCreatedAt()
            );
        }
    }
}
