package com.vintly.domain.report.integration;

import com.vintly.TestContainerConfig;
import com.vintly.application.report.ReportFacade;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.interfaces.report.ReportException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 신고 접수 통합 테스트.
 *
 * 컨트롤러 아래 전 구간(Facade - Service - Repository)을 실제 MariaDB에 대고 확인한다.
 * 리포지토리 단독 테스트가 보지 못하는 것, 즉 대상 존재 확인과 자기 콘텐츠 판정이 실제
 * board / board_comment / vintage_comment 데이터에 대해 동작하는지가 검증 대상이다.
 *
 * 실행에는 Docker가 필요하다. 격리는 클래스 레벨 Transactional 롤백.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest extends TestContainerConfig {

    @Autowired private ReportFacade reportFacade;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardCommentRepository boardCommentRepository;
    @Autowired private VintageRepository vintageRepository;
    @Autowired private VintageCommentRepository vintageCommentRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Member reporter;
    private Member author;
    private Board board;
    private BoardComment boardComment;
    private VintageComment vintageComment;

    @BeforeEach
    void setUp() {
        reporter = memberRepository.save(newMember("reporter@test.local", "reporterNick"));
        author = memberRepository.save(newMember("author@test.local", "authorNick"));

        board = boardRepository.save(Board.create(author, "신고 대상 글", "본문"));
        boardComment = boardCommentRepository.save(BoardComment.createRoot(board, author, "신고 대상 댓글"));

        Vintage vintage = vintageRepository.save(Vintage.create(
                "report-target-vintage", "서울특별시", "중구", "세종대로 110",
                new BigDecimal("37.566500"), new BigDecimal("126.978000"), null));
        vintageComment = vintageCommentRepository.save(
                VintageComment.createRoot(vintage, author, "신고 대상 매장 댓글"));

        entityManager.flush();
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("남이 쓴 게시글을 신고하면 PENDING 상태로 저장된다.")
    void reportsBoard() {
        // given

        // when
        Long reportId = reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD, board.getBoardId(), ReportReason.ABUSE, "욕설이 있습니다.");

        // then
        assertThat(reportId).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM report WHERE report_id = ?", String.class, reportId))
                .isEqualTo("PENDING");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reporter_id FROM report WHERE report_id = ?", Long.class, reportId))
                .isEqualTo(reporter.getMemberId());
    }

    @Test
    @DisplayName("게시판 댓글과 매장 댓글도 같은 엔드포인트로 접수된다.")
    void reportsBothCommentTypes() {
        // given

        // when
        reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD_COMMENT, boardComment.getBoardCommentId(), ReportReason.SPAM, null);
        reportFacade.report(reporter.getMemberId(),
                ReportTargetType.VINTAGE_COMMENT, vintageComment.getVintageCommentId(), ReportReason.FLOOD, null);

        // then
        List<ReportInfo.My> myReports = reportFacade.findMyReports(reporter.getMemberId());
        assertThat(myReports).extracting(ReportInfo.My::targetType)
                .containsExactlyInAnyOrder(ReportTargetType.BOARD_COMMENT, ReportTargetType.VINTAGE_COMMENT);
    }

    @Test
    @DisplayName("본인이 작성한 게시글은 신고할 수 없고 아무것도 저장되지 않는다.")
    void cannotReportOwnBoard() {
        // given

        // when & then
        assertThatThrownBy(() -> reportFacade.report(author.getMemberId(),
                ReportTargetType.BOARD, board.getBoardId(), ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.SelfReportException.class);

        assertThat(countReportsOf(author)).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 대상은 신고할 수 없다.")
    void cannotReportMissingTarget() {
        // given
        Long missingBoardId = board.getBoardId() + 9999;

        // when & then
        assertThatThrownBy(() -> reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD, missingBoardId, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.ReportTargetNotFoundException.class);

        assertThat(countReportsOf(reporter)).isZero();
    }

    @Test
    @DisplayName("같은 대상을 두 번 신고하면 두 번째는 거부되고 한 건만 남는다.")
    void cannotReportSameTargetTwice() {
        // given
        reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD, board.getBoardId(), ReportReason.ABUSE, null);

        // when & then
        assertThatThrownBy(() -> reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD, board.getBoardId(), ReportReason.SPAM, "사유만 바꿔 재신고"))
                .isInstanceOf(ReportException.DuplicateReportException.class);

        assertThat(countReportsOf(reporter)).isEqualTo(1);
    }

    @Test
    @DisplayName("탈퇴 회원이 남긴 매장 댓글도 신고할 수 있다.")
    void reportsVintageCommentOfWithdrawnMember() {
        // given - 탈퇴 시 vintage_comment.member_id는 null이 되고 댓글은 남는다
        jdbcTemplate.update("UPDATE vintage_comment SET member_id = NULL, author_nickname = ? "
                + "WHERE vintage_comment_id = ?", "del_" + author.getMemberId(), vintageComment.getVintageCommentId());
        entityManager.clear();

        // when
        Long reportId = reportFacade.report(reporter.getMemberId(),
                ReportTargetType.VINTAGE_COMMENT, vintageComment.getVintageCommentId(), ReportReason.SPAM, null);

        // then
        assertThat(reportId).isNotNull();
        assertThat(countReportsOf(reporter)).isEqualTo(1);
    }

    @Test
    @DisplayName("내 신고 내역은 다른 사람의 신고를 포함하지 않는다.")
    void myReportsExcludeOthers() {
        // given - 신고자의 글을 작성자가 맞신고한 상황
        Board boardOfReporter = boardRepository.save(Board.create(reporter, "신고자가 쓴 글", "본문"));
        entityManager.flush();

        reportFacade.report(reporter.getMemberId(),
                ReportTargetType.BOARD, board.getBoardId(), ReportReason.ABUSE, null);
        reportFacade.report(author.getMemberId(),
                ReportTargetType.BOARD, boardOfReporter.getBoardId(), ReportReason.SPAM, null);

        // when
        List<ReportInfo.My> myReports = reportFacade.findMyReports(reporter.getMemberId());

        // then
        assertThat(myReports).hasSize(1);
        assertThat(myReports.get(0).targetType()).isEqualTo(ReportTargetType.BOARD);
        assertThat(myReports.get(0).status()).isEqualTo(ReportStatus.PENDING);
    }

    private int countReportsOf(Member member) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM report WHERE reporter_id = ?", Integer.class, member.getMemberId());
        return count != null ? count : 0;
    }
}
