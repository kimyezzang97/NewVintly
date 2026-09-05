package com.vintly.domain.report.repo;

import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.entity.Report;

import java.util.List;

public interface ReportRepository {

    // 신고 접수 저장. 중복이면 DataIntegrityViolationException 이 즉시 발생한다 (구현체 주석 참고)
    Report save(Report report);

    // 중복 신고 사전 확인 (신고자 + 대상 종류 + 대상 ID 조합)
    boolean existsByReporterIdAndTarget(Long reporterId, ReportTargetType targetType, Long targetId);

    // 내 신고 내역 (최신순)
    List<Report> findAllByReporterId(Long reporterId);
}
