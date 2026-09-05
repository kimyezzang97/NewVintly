package com.vintly.domain.block.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.block.repo.MemberBlockRepository;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.domain.vintagecomment.service.VintageCommentService;
import com.vintly.interfaces.block.MemberBlockException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 나를 차단한 회원의 글에 댓글을 달 수 없다 (설계 결정 6번).
 *
 * 차단은 단방향 표시 차단이라, A 가 나를 차단해도 A 의 글은 내게 그대로 보인다. 검사가 없으면
 * 계속 댓글을 달 수 있고 다른 사람들에게는 그게 보인다. 결정 6번이 막으려는 경로가 이것이다.
 *
 * 검사 방향에 주의한다. 막아야 하는 것은 "내가 차단한 사람"이 아니라 "나를 차단한 사람"이다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentWriteBlockIntegrationTest extends TestContainerConfig {

    @Autowired private BoardCommentService boardCommentService;
    @Autowired private VintageCommentService vintageCommentService;
    @Autowired private MemberBlockRepository memberBlockRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardCommentRepository boardCommentRepository;
    @Autowired private VintageRepository vintageRepository;
    @Autowired private VintageCommentRepository vintageCommentRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;

    private Member author;      // 글쓴이 (나를 차단한 사람)
    private Member me;          // 댓글을 달려는 사람
    private Member bystander;   // 아무 관계 없는 사람

    private Long boardId;
    private Long authorRootCommentId;
    private Long bystanderRootCommentId;

    private Long vintageId;
    private Long vintageAuthorCommentId;

    @BeforeEach
    void setUp() {
        author = memberRepository.save(newMember("cw-author@test.local", "cwAuthor"));
        me = memberRepository.save(newMember("cw-me@test.local", "cwMe"));
        bystander = memberRepository.save(newMember("cw-bystander@test.local", "cwBystander"));

        Board board = boardRepository.save(Board.create(author, "글쓴이의 글", "본문"));
        boardId = board.getBoardId();
        authorRootCommentId = boardCommentRepository.save(
                BoardComment.createRoot(board, author, "글쓴이 댓글")).getBoardCommentId();
        bystanderRootCommentId = boardCommentRepository.save(
                BoardComment.createRoot(board, bystander, "제3자 댓글")).getBoardCommentId();

        Vintage vintage = vintageRepository.save(Vintage.create(
                "cw-vintage", "서울특별시", "중구", "세종대로 110",
                new BigDecimal("37.566500"), new BigDecimal("126.978000"), null));
        vintageId = vintage.getVintageId();
        vintageAuthorCommentId = vintageCommentRepository.save(
                VintageComment.createRoot(vintage, author, "글쓴이 매장 댓글")).getVintageCommentId();

        entityManager.flush();
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    private void authorBlocksMe() {
        memberBlockRepository.save(MemberBlock.create(
                memberRepository.findById(author.getMemberId()).orElseThrow(),
                memberRepository.findById(me.getMemberId()).orElseThrow()));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("나를 차단한 회원의 게시글에는 댓글을 달 수 없다.")
    void cannotCommentOnBoardOfMemberWhoBlockedMe() {
        // given
        authorBlocksMe();

        // when & then
        assertThatThrownBy(() -> boardCommentService.create(boardId, me.getMemberId(), 0L, "댓글"))
                .isInstanceOf(MemberBlockException.BlockedByAuthorException.class);
    }

    @Test
    @DisplayName("나를 차단한 회원의 댓글에는 대댓글을 달 수 없다.")
    void cannotReplyToCommentOfMemberWhoBlockedMe() {
        // given - 글쓴이가 아닌 제3자의 글이라도 부모 댓글 작성자가 나를 차단했으면 막는다
        Board otherBoard = boardRepository.save(Board.create(bystander, "제3자 글", "본문"));
        Long otherBoardId = otherBoard.getBoardId();
        Long authorCommentOnOtherBoard = boardCommentRepository.save(
                BoardComment.createRoot(otherBoard, author, "글쓴이 댓글")).getBoardCommentId();
        entityManager.flush();
        authorBlocksMe();

        // when & then
        assertThatThrownBy(() -> boardCommentService.create(
                otherBoardId, me.getMemberId(), authorCommentOnOtherBoard, "대댓글"))
                .isInstanceOf(MemberBlockException.BlockedByAuthorException.class);
    }

    @Test
    @DisplayName("차단당하지 않았으면 댓글을 정상적으로 단다.")
    void canCommentWhenNotBlocked() {
        // given - 차단 없음

        // when & then
        assertThatCode(() -> boardCommentService.create(boardId, me.getMemberId(), 0L, "댓글"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("내가 차단한 사람의 글에는 내가 댓글을 달 수 있다. 방향을 뒤집으면 안 된다.")
    void canCommentOnBoardOfMemberIBlocked() {
        // given - 내가 글쓴이를 차단했다 (반대 방향)
        memberBlockRepository.save(MemberBlock.create(
                memberRepository.findById(me.getMemberId()).orElseThrow(),
                memberRepository.findById(author.getMemberId()).orElseThrow()));
        entityManager.flush();
        entityManager.clear();

        // when & then - 막히면 방향이 뒤집힌 것이다
        assertThatCode(() -> boardCommentService.create(boardId, me.getMemberId(), 0L, "댓글"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("나를 차단하지 않은 제3자의 댓글에는 대댓글을 달 수 있다.")
    void canReplyToBystanderComment() {
        // given
        authorBlocksMe();

        // when & then - 글쓴이가 나를 차단했으므로 이 게시글 자체는 막힌다.
        // 제3자 댓글 검사만 따로 보기 위해 제3자의 글에서 확인한다.
        Board otherBoard = boardRepository.save(Board.create(bystander, "제3자 글", "본문"));
        Long otherBoardId = otherBoard.getBoardId();
        Long bystanderComment = boardCommentRepository.save(
                BoardComment.createRoot(otherBoard, bystander, "제3자 댓글")).getBoardCommentId();
        entityManager.flush();

        assertThatCode(() -> boardCommentService.create(
                otherBoardId, me.getMemberId(), bystanderComment, "대댓글"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("매장 댓글은 글쓴이가 없으므로 최상위 댓글은 막지 않는다.")
    void vintageRootCommentIsNeverBlocked() {
        // given
        authorBlocksMe();

        // when & then - 매장은 특정 회원의 소유가 아니다
        assertThatCode(() -> vintageCommentService.create(vintageId, me.getMemberId(), 0L, "매장 댓글"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("나를 차단한 회원의 매장 댓글에는 대댓글을 달 수 없다.")
    void cannotReplyToVintageCommentOfMemberWhoBlockedMe() {
        // given
        authorBlocksMe();

        // when & then
        assertThatThrownBy(() -> vintageCommentService.create(
                vintageId, me.getMemberId(), vintageAuthorCommentId, "대댓글"))
                .isInstanceOf(MemberBlockException.BlockedByAuthorException.class);
    }
}
