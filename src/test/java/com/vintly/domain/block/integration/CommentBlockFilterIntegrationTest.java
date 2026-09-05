package com.vintly.domain.block.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.domain.vintagecomment.service.VintageCommentService;
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

/**
 * 댓글 조회의 차단 필터 통합 테스트.
 *
 * 설계 문서(docs/design/block.md)의 함정 1과 3을 고정한다.
 *   함정 1 — leftJoin 으로 회원을 붙이면 탈퇴자 댓글의 member_id 가 NULL 이 된다.
 *            NOT IN 이 NULL 을 걸러내므로 차단과 무관한 탈퇴자 댓글이 통째로 사라진다.
 *   함정 3 — 결정 7번에 따라 차단한 사람의 최상위 댓글에 달린 남의 대댓글도 함께 가린다.
 *            작성자만 봐서는 알 수 없어 부모를 보는 서브쿼리가 필요하다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentBlockFilterIntegrationTest extends TestContainerConfig {

    @Autowired private BoardCommentService boardCommentService;
    @Autowired private VintageCommentService vintageCommentService;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardCommentRepository boardCommentRepository;
    @Autowired private VintageRepository vintageRepository;
    @Autowired private VintageCommentRepository vintageCommentRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Member blockedAuthor;
    private Member innocent;
    private Member withdrawn;

    private Long boardId;
    private Long blockedRootCommentId;
    private Long replyToBlockedRootId;
    private Long innocentRootCommentId;
    private Long withdrawnCommentId;

    private Long vintageId;
    private Long vintageBlockedCommentId;
    private Long vintageWithdrawnCommentId;

    @BeforeEach
    void setUp() {
        blockedAuthor = memberRepository.save(newMember("cb-blocked@test.local", "cbBlocked"));
        innocent = memberRepository.save(newMember("cb-innocent@test.local", "cbInnocent"));
        withdrawn = memberRepository.save(newMember("cb-withdrawn@test.local", "cbWithdrawn"));

        Board board = boardRepository.save(Board.create(innocent, "글", "본문"));
        boardId = board.getBoardId();

        BoardComment blockedRoot = boardCommentRepository.save(
                BoardComment.createRoot(board, blockedAuthor, "차단 대상의 최상위 댓글"));
        blockedRootCommentId = blockedRoot.getBoardCommentId();

        BoardComment innocentRoot = boardCommentRepository.save(
                BoardComment.createRoot(board, innocent, "멀쩡한 최상위 댓글"));
        innocentRootCommentId = innocentRoot.getBoardCommentId();

        // 차단 대상의 최상위 댓글에 달린, 차단과 무관한 사람의 대댓글
        replyToBlockedRootId = boardCommentRepository.save(
                BoardComment.createReply(board, innocent, blockedRootCommentId, "남이 단 대댓글")).getBoardCommentId();

        withdrawnCommentId = boardCommentRepository.save(
                BoardComment.createRoot(board, withdrawn, "탈퇴자 댓글")).getBoardCommentId();

        Vintage vintage = vintageRepository.save(Vintage.create(
                "cb-vintage", "서울특별시", "중구", "세종대로 110",
                new BigDecimal("37.566500"), new BigDecimal("126.978000"), null));
        vintageId = vintage.getVintageId();

        vintageBlockedCommentId = vintageCommentRepository.save(
                VintageComment.createRoot(vintage, blockedAuthor, "차단 대상의 매장 댓글")).getVintageCommentId();
        vintageWithdrawnCommentId = vintageCommentRepository.save(
                VintageComment.createRoot(vintage, withdrawn, "탈퇴자 매장 댓글")).getVintageCommentId();

        entityManager.flush();

        // 탈퇴 재현. board_comment 는 orphan 값이 남고, vintage_comment 는 member_id 가 null 이 된다.
        jdbcTemplate.update("UPDATE vintage_comment SET member_id = NULL, author_nickname = ? "
                + "WHERE vintage_comment_id = ?", "del_" + withdrawn.getMemberId(), vintageWithdrawnCommentId);
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", withdrawn.getMemberId());
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    private List<Long> boardCommentIds(List<Long> blockedIds) {
        return boardCommentService.findCommentsByBoardId(boardId, blockedIds).stream()
                .map(BoardInfo.Comment::commentId)
                .toList();
    }

    private List<Long> vintageCommentIds(List<Long> blockedIds) {
        return vintageCommentService.findCommentsByVintageId(vintageId, blockedIds).stream()
                .map(VintageInfo.Comment::commentId)
                .toList();
    }

    @Test
    @DisplayName("차단한 회원의 게시판 댓글은 사라진다.")
    void blockedAuthorCommentIsHidden() {
        // given

        // when
        List<Long> ids = boardCommentIds(List.of(blockedAuthor.getMemberId()));

        // then
        assertThat(ids).doesNotContain(blockedRootCommentId);
    }

    @Test
    @DisplayName("함정 3 — 차단한 회원의 최상위 댓글에 달린 남의 대댓글도 함께 사라진다.")
    void replyToBlockedRootIsAlsoHidden() {
        // given - 대댓글 작성자는 차단 대상이 아니다

        // when
        List<Long> ids = boardCommentIds(List.of(blockedAuthor.getMemberId()));

        // then - 작성자만 보고 거르면 이 대댓글이 부모 없이 남는다
        assertThat(ids).doesNotContain(replyToBlockedRootId);
    }

    @Test
    @DisplayName("차단하지 않은 회원의 최상위 댓글은 그대로 보인다.")
    void innocentRootCommentRemains() {
        // given

        // when
        List<Long> ids = boardCommentIds(List.of(blockedAuthor.getMemberId()));

        // then
        assertThat(ids).contains(innocentRootCommentId);
    }

    @Test
    @DisplayName("함정 1 — 탈퇴 회원의 게시판 댓글은 차단과 무관하므로 남아야 한다.")
    void withdrawnBoardCommentSurvives() {
        // given - 작성자 행이 없어 leftJoin 결과의 member_id 가 NULL 이다

        // when
        List<Long> ids = boardCommentIds(List.of(blockedAuthor.getMemberId()));

        // then
        assertThat(ids).contains(withdrawnCommentId);
    }

    @Test
    @DisplayName("차단이 없으면 게시판 댓글이 모두 보인다.")
    void noBlockShowsAllBoardComments() {
        // given

        // when
        List<Long> ids = boardCommentIds(List.of());

        // then
        assertThat(ids).contains(blockedRootCommentId, innocentRootCommentId,
                replyToBlockedRootId, withdrawnCommentId);
    }

    @Test
    @DisplayName("차단한 회원의 매장 댓글은 사라진다.")
    void blockedAuthorVintageCommentIsHidden() {
        // given

        // when
        List<Long> ids = vintageCommentIds(List.of(blockedAuthor.getMemberId()));

        // then
        assertThat(ids).doesNotContain(vintageBlockedCommentId);
    }

    @Test
    @DisplayName("함정 1 — 탈퇴 회원의 매장 댓글은 member_id가 null이어도 남아야 한다.")
    void withdrawnVintageCommentSurvives() {
        // given - vintage_comment.member_id 는 탈퇴 시 실제로 null 이 된다

        // when
        List<Long> ids = vintageCommentIds(List.of(blockedAuthor.getMemberId()));

        // then
        assertThat(ids).contains(vintageWithdrawnCommentId);
    }

    @Test
    @DisplayName("차단이 없으면 매장 댓글이 모두 보인다.")
    void noBlockShowsAllVintageComments() {
        // given

        // when
        List<Long> ids = vintageCommentIds(List.of());

        // then
        assertThat(ids).contains(vintageBlockedCommentId, vintageWithdrawnCommentId);
    }
}
