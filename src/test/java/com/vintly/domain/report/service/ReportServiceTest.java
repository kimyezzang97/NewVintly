package com.vintly.domain.report.service;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.domain.report.entity.Report;
import com.vintly.domain.report.repo.ReportRepository;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.report.ReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private MemberRepository memberRepository;
    @InjectMocks private ReportService reportService;

    private static final Long REPORTER_ID = 1L;

    private Member reporter() {
        return new Member(REPORTER_ID, "reporter@test.com", "password", "reporterNick",
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("신고를 접수하면 PENDING 상태로 저장된다.")
    void createSavesReportAsPending() {
        // given
        Member reporter = reporter();
        given(memberRepository.findById(REPORTER_ID)).willReturn(Optional.of(reporter));
        given(reportRepository.existsByReporterIdAndTarget(REPORTER_ID, ReportTargetType.BOARD, 100L))
                .willReturn(false);
        given(reportRepository.save(any(Report.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        reportService.create(REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.");

        // then
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report saved = captor.getValue();
        assertThat(saved.getReporter()).isSameAs(reporter);
        assertThat(saved.getTargetType()).isEqualTo(ReportTargetType.BOARD);
        assertThat(saved.getTargetId()).isEqualTo(100L);
        assertThat(saved.getReason()).isEqualTo(ReportReason.ABUSE);
        assertThat(saved.getDetail()).isEqualTo("욕설이 있습니다.");
        assertThat(saved.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("이미 신고한 대상을 다시 신고하면 저장하지 않고 예외가 발생한다.")
    void createRejectsDuplicateReport() {
        // given
        given(reportRepository.existsByReporterIdAndTarget(REPORTER_ID, ReportTargetType.BOARD, 100L))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> reportService.create(
                REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.DuplicateReportException.class);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("동시 요청으로 선체크를 통과해도 유니크 제약 위반은 중복 신고 예외로 변환된다.")
    void createTranslatesConstraintViolationToDuplicateException() {
        // given
        given(memberRepository.findById(REPORTER_ID)).willReturn(Optional.of(reporter()));
        given(reportRepository.existsByReporterIdAndTarget(REPORTER_ID, ReportTargetType.BOARD, 100L))
                .willReturn(false);
        given(reportRepository.save(any(Report.class)))
                .willThrow(new DataIntegrityViolationException("uk_report_reporter_target"));

        // when & then
        assertThatThrownBy(() -> reportService.create(
                REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.DuplicateReportException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 신고하면 회원 조회 예외가 발생한다.")
    void createRejectsUnknownReporter() {
        // given
        given(reportRepository.existsByReporterIdAndTarget(anyLong(), any(ReportTargetType.class), anyLong()))
                .willReturn(false);
        given(memberRepository.findById(REPORTER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.create(
                REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null))
                .isInstanceOf(MemberException.MemberNotFoundException.class);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("내 신고 내역은 접수 정보만 담아 반환한다.")
    void findMyReportsReturnsReportInfo() {
        // given
        Report report = Report.create(reporter(), ReportTargetType.VINTAGE_COMMENT, 42L, ReportReason.SPAM, "광고입니다.");
        given(reportRepository.findAllByReporterId(REPORTER_ID)).willReturn(List.of(report));

        // when
        List<ReportInfo.My> myReports = reportService.findMyReports(REPORTER_ID);

        // then
        assertThat(myReports).hasSize(1);
        ReportInfo.My my = myReports.get(0);
        assertThat(my.targetType()).isEqualTo(ReportTargetType.VINTAGE_COMMENT);
        assertThat(my.targetId()).isEqualTo(42L);
        assertThat(my.reason()).isEqualTo(ReportReason.SPAM);
        assertThat(my.detail()).isEqualTo("광고입니다.");
        assertThat(my.status()).isEqualTo(ReportStatus.PENDING);
    }
}
