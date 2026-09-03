package com.vintly.domain.report.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.entity.Report;
import com.vintly.domain.report.repo.ReportRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 신고 접수 리포지토리 통합 테스트.
 *
 * 단위 테스트로는 확인할 수 없는 두 가지를 실제 MariaDB 스키마에 대고 검증한다.
 *   - 복합 유니크 제약이 실제 DDL 로 나가는가 — (reporter_id, target_type, target_id).
 *       엔티티에 어노테이션만 붙고 스키마에 반영되지 않으면 중복 신고가 그대로 쌓인다.
 *   - enum 이 문자열로 저장되는가 — 관리자가 DB 를 직접 조회해 처리하므로
 *       0, 1 이 아니라 BOARD, PENDING 이 보여야 한다.
 *
 * 실행에는 Docker 가 필요하다. 격리는 클래스 레벨 Transactional 롤백.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportRepositoryIntegrationTest extends TestContainerConfig {

    @Autowired private ReportRepository reportRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Member reporter;
    private Member otherReporter;

    @BeforeEach
    void setUp() {
        reporter = memberRepository.save(newMember("reporter@test.com", "reporterNick"));
        otherReporter = memberRepository.save(newMember("other@test.com", "otherNick"));
        entityManager.flush();
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("신고를 저장하면 신고자별 조회로 다시 읽힌다.")
    void savedReportIsFoundByReporter() {
        // given
        Report report = Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.");

        // when
        reportRepository.save(report);
        entityManager.clear();

        // then
        List<Report> found = reportRepository.findAllByReporterId(reporter.getMemberId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTargetType()).isEqualTo(ReportTargetType.BOARD);
        assertThat(found.get(0).getTargetId()).isEqualTo(100L);
        assertThat(found.get(0).getReason()).isEqualTo(ReportReason.ABUSE);
        assertThat(found.get(0).getDetail()).isEqualTo("욕설이 있습니다.");
        assertThat(found.get(0).getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("enum 세 개는 숫자가 아니라 문자열로 저장된다.")
    void enumsArePersistedAsStrings() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.VINTAGE_COMMENT, 55L, ReportReason.SPAM, null));

        // when
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT target_type, reason, status FROM report WHERE target_id = 55");

        // then
        assertThat(row.get("target_type")).isEqualTo("VINTAGE_COMMENT");
        assertThat(row.get("reason")).isEqualTo("SPAM");
        assertThat(row.get("status")).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("같은 신고자가 같은 대상을 다시 신고하면 유니크 제약에 걸린다.")
    void duplicateReportIsRejectedByUniqueConstraint() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null));

        // when
        Report duplicate = Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.SPAM, "사유만 바꿔 재신고");

        // then
        assertThatThrownBy(() -> reportRepository.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("target_id가 같아도 대상 종류가 다르면 별개 신고로 저장된다.")
    void sameTargetIdWithDifferentTypeIsAllowed() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null));

        // when
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD_COMMENT, 100L, ReportReason.ABUSE, null));
        entityManager.clear();

        // then
        assertThat(reportRepository.findAllByReporterId(reporter.getMemberId())).hasSize(2);
    }

    @Test
    @DisplayName("신고자가 다르면 같은 대상을 각각 신고할 수 있다.")
    void differentReportersCanReportSameTarget() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null));

        // when
        reportRepository.save(Report.create(otherReporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null));
        entityManager.clear();

        // then
        assertThat(reportRepository.findAllByReporterId(reporter.getMemberId())).hasSize(1);
        assertThat(reportRepository.findAllByReporterId(otherReporter.getMemberId())).hasSize(1);
    }

    @Test
    @DisplayName("중복 신고 여부는 신고자·대상 종류·대상 ID 조합으로 판정한다.")
    void existsChecksReporterAndTargetCombination() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null));

        // when
        boolean sameCombination = reportRepository.existsByReporterIdAndTarget(
                reporter.getMemberId(), ReportTargetType.BOARD, 100L);
        boolean otherType = reportRepository.existsByReporterIdAndTarget(
                reporter.getMemberId(), ReportTargetType.BOARD_COMMENT, 100L);
        boolean otherReporterSameTarget = reportRepository.existsByReporterIdAndTarget(
                otherReporter.getMemberId(), ReportTargetType.BOARD, 100L);

        // then
        assertThat(sameCombination).isTrue();
        assertThat(otherType).isFalse();
        assertThat(otherReporterSameTarget).isFalse();
    }

    @Test
    @DisplayName("내 신고 내역은 최신순으로 반환된다.")
    void reportsOfReporterAreOrderedByNewestFirst() {
        // given
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 1L, ReportReason.ABUSE, null));
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 2L, ReportReason.SPAM, null));
        reportRepository.save(Report.create(reporter, ReportTargetType.BOARD, 3L, ReportReason.FLOOD, null));
        entityManager.clear();

        // when
        List<Report> found = reportRepository.findAllByReporterId(reporter.getMemberId());

        // then
        assertThat(found).extracting(Report::getTargetId).containsExactly(3L, 2L, 1L);
    }
}
