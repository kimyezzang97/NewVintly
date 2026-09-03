package com.vintly.application.report;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.service.ReportService;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.interfaces.report.ReportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportFacadeTest {

    @Mock private ReportService reportService;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardCommentRepository boardCommentRepository;
    @Mock private VintageCommentRepository vintageCommentRepository;
    @InjectMocks private ReportFacade reportFacade;

    private static final Long REPORTER_ID = 1L;
    private static final Long AUTHOR_ID = 2L;

    private Member member(Long memberId, String nickname) {
        return new Member(memberId, memberId + "@test.com", "password", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("남이 쓴 게시글은 신고가 접수된다.")
    void reportsBoardWrittenByOthers() {
        // given
        Board board = Board.create(member(AUTHOR_ID, "authorNick"), "제목", "본문");
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));

        // when
        reportFacade.report(REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.");

        // then
        verify(reportService).create(REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, "욕설이 있습니다.");
    }

    @Test
    @DisplayName("내가 쓴 게시글은 신고할 수 없다.")
    void cannotReportOwnBoard() {
        // given
        Board board = Board.create(member(REPORTER_ID, "myNick"), "제목", "본문");
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.BOARD, 100L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.SelfReportException.class);

        verify(reportService, never()).create(anyLong(), any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("내가 쓴 게시판 댓글은 신고할 수 없다.")
    void cannotReportOwnBoardComment() {
        // given
        Member me = member(REPORTER_ID, "myNick");
        BoardComment comment = BoardComment.createRoot(Board.create(me, "제목", "본문"), me, "내 댓글");
        given(boardCommentRepository.findById(200L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.BOARD_COMMENT, 200L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.SelfReportException.class);
    }

    @Test
    @DisplayName("내가 쓴 매장 댓글은 신고할 수 없다.")
    void cannotReportOwnVintageComment() {
        // given
        VintageComment comment = new VintageComment(300L, null, member(REPORTER_ID, "myNick"), 0L, "내 댓글", "myNick");
        given(vintageCommentRepository.findById(300L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.VINTAGE_COMMENT, 300L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.SelfReportException.class);
    }

    @Test
    @DisplayName("탈퇴 회원이 남긴 매장 댓글은 작성자가 없어도 신고할 수 있다.")
    void reportsVintageCommentOfWithdrawnMember() {
        // given - 탈퇴 시 vintage_comment.member_id는 null이 된다
        VintageComment comment = new VintageComment(300L, null, null, 0L, "탈퇴자 댓글", "del_9");
        given(vintageCommentRepository.findById(300L)).willReturn(Optional.of(comment));

        // when
        reportFacade.report(REPORTER_ID, ReportTargetType.VINTAGE_COMMENT, 300L, ReportReason.SPAM, null);

        // then
        verify(reportService).create(REPORTER_ID, ReportTargetType.VINTAGE_COMMENT, 300L, ReportReason.SPAM, null);
    }

    @Test
    @DisplayName("존재하지 않는 게시글은 신고할 수 없다.")
    void cannotReportMissingBoard() {
        // given
        given(boardRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.BOARD, 999L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.ReportTargetNotFoundException.class);

        verify(reportService, never()).create(anyLong(), any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 게시판 댓글은 신고할 수 없다.")
    void cannotReportMissingBoardComment() {
        // given
        given(boardCommentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.BOARD_COMMENT, 999L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.ReportTargetNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 매장 댓글은 신고할 수 없다.")
    void cannotReportMissingVintageComment() {
        // given
        given(vintageCommentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportFacade.report(
                REPORTER_ID, ReportTargetType.VINTAGE_COMMENT, 999L, ReportReason.ABUSE, null))
                .isInstanceOf(ReportException.ReportTargetNotFoundException.class);
    }

    @Test
    @DisplayName("내 신고 내역 조회는 서비스에 그대로 위임한다.")
    void findMyReportsDelegatesToService() {
        // given

        // when
        reportFacade.findMyReports(REPORTER_ID);

        // then
        verify(reportService).findMyReports(eq(REPORTER_ID));
    }
}
