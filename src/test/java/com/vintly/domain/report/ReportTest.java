package com.vintly.domain.report;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.report.entity.Report;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportTest {

    private Member reporter() {
        return new Member(null, "reporter@test.com", "password", "reporterNick",
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("신고를 접수하면 상태가 PENDING으로 시작한다.")
    void createdReportStartsAsPending() {
        // given
        Member reporter = reporter();

        // when
        Report report = Report.create(reporter, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.");

        // then
        Assertions.assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("신고 접수 시 전달한 신고자·대상·사유가 그대로 담긴다.")
    void createdReportKeepsGivenValues() {
        // given
        Member reporter = reporter();

        // when
        Report report = Report.create(reporter, ReportTargetType.VINTAGE_COMMENT, 42L, ReportReason.SPAM, "광고 댓글입니다.");

        // then
        Assertions.assertThat(report.getReporter()).isSameAs(reporter);
        Assertions.assertThat(report.getTargetType()).isEqualTo(ReportTargetType.VINTAGE_COMMENT);
        Assertions.assertThat(report.getTargetId()).isEqualTo(42L);
        Assertions.assertThat(report.getReason()).isEqualTo(ReportReason.SPAM);
        Assertions.assertThat(report.getDetail()).isEqualTo("광고 댓글입니다.");
    }

    @Test
    @DisplayName("상세 사유는 선택값이므로 없이도 접수된다.")
    void detailIsOptional() {
        // given
        Member reporter = reporter();

        // when
        Report report = Report.create(reporter, ReportTargetType.BOARD_COMMENT, 7L, ReportReason.OBSCENE, null);

        // then
        Assertions.assertThat(report.getDetail()).isNull();
        Assertions.assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("신고 대상 종류는 게시글·게시판댓글·매장댓글 세 가지다.")
    void targetTypeCoversThreeContents() {
        // given

        // when
        ReportTargetType[] types = ReportTargetType.values();

        // then
        Assertions.assertThat(types).containsExactlyInAnyOrder(
                ReportTargetType.BOARD, ReportTargetType.BOARD_COMMENT, ReportTargetType.VINTAGE_COMMENT);
    }
}
