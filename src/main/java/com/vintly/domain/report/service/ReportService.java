package com.vintly.domain.report.service;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.domain.report.entity.Report;
import com.vintly.domain.report.repo.ReportRepository;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.report.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /**
     * 신고 접수.
     *
     * 중복 신고는 두 겹으로 막는다. 선체크가 정상 흐름을 걸러 명확한 예외를 주고, 동시 요청으로
     * 양쪽 모두 선체크를 통과한 경우에는 유니크 제약이 잡는다. 후자를 그대로 흘리면 500 이 나가므로
     * 같은 예외로 변환해 409 로 응답한다.
     *
     * 대상 존재 확인과 자기 콘텐츠 여부 판정은 대상 도메인을 알아야 하므로 Facade 가 맡는다.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long reporterId, ReportTargetType targetType, Long targetId,
                       ReportReason reason, String detail) {
        if (reportRepository.existsByReporterIdAndTarget(reporterId, targetType, targetId)) {
            throw new ReportException.DuplicateReportException();
        }

        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        try {
            return reportRepository.save(Report.create(reporter, targetType, targetId, reason, detail))
                    .getReportId();
        } catch (DataIntegrityViolationException e) {
            throw new ReportException.DuplicateReportException();
        }
    }

    @Transactional(readOnly = true)
    public List<ReportInfo.My> findMyReports(Long reporterId) {
        return reportRepository.findAllByReporterId(reporterId).stream()
                .map(ReportInfo.My::from)
                .toList();
    }
}
