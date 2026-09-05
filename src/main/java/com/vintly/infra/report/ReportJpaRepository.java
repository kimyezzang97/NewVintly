package com.vintly.infra.report;

import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportJpaRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterMemberIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    List<Report> findAllByReporterMemberIdOrderByReportIdDesc(Long reporterId);
}
