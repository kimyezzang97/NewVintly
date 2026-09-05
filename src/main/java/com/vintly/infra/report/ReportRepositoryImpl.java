package com.vintly.infra.report;

import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.entity.Report;
import com.vintly.domain.report.repo.ReportRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportJpaRepository jpaRepository;

    public ReportRepositoryImpl(ReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * 신고 접수 저장.
     *
     * save 가 아니라 saveAndFlush 를 쓴다. 중복 신고는 서비스에서 먼저 걸러내지만,
     * 두 요청이 동시에 들어오면 양쪽 모두 "없음"을 보고 통과하는 창이 남는다. 그 경우 유니크 제약이
     * 최후 방어선이 되는데, 지연 플러시로 두면 위반이 트랜잭션 커밋 시점에 터진다. 그때는 서비스를
     * 이미 빠져나온 뒤라 409 로 변환할 수 없고 500 이 나간다. 저장 시점에 플러시해 서비스가 잡을 수
     * 있게 한다.
     */
    @Override
    public Report save(Report report) {
        return jpaRepository.saveAndFlush(report);
    }

    @Override
    public boolean existsByReporterIdAndTarget(Long reporterId, ReportTargetType targetType, Long targetId) {
        return jpaRepository.existsByReporterMemberIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    // 최신순 정렬에 createdAt 대신 PK 를 쓴다. 같은 초에 접수된 신고끼리 순서가 흔들리지 않게 하기 위함.
    @Override
    public List<Report> findAllByReporterId(Long reporterId) {
        return jpaRepository.findAllByReporterMemberIdOrderByReportIdDesc(reporterId);
    }
}
