package com.vintly.domain.block.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.domain.block.service.MemberBlockService;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.domain.board.service.BoardService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.interfaces.block.MemberBlockException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 차단 전체 흐름 통합 테스트.
 *
 * 개별 규칙은 다른 통합 테스트가 덮는다. 여기서는 차단하고, 화면에서 사라지고, 해제하면 다시
 * 보이기까지가 실제로 이어지는지를 한 번에 확인한다. 단계별로 통과해도 연결이 끊기면 기능이
 * 성립하지 않기 때문이다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BlockLifecycleIntegrationTest extends TestContainerConfig {

    @Autowired private MemberBlockService memberBlockService;
    @Autowired private BoardService boardService;
    @Autowired private BoardCommentService boardCommentService;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardCommentRepository boardCommentRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;

    private static final Pageable PAGE = PageRequest.of(0, 20);

    private Member me;
    private Member noisy;
    private Long noisyBoardId;
    private Long noisyCommentId;
    private Long myBoardId;

    @BeforeEach
    void setUp() {
        me = memberRepository.save(newMember("lc-me@test.local", "lcMe"));
        noisy = memberRepository.save(newMember("lc-noisy@test.local", "lcNoisy"));

        Board noisyBoard = boardRepository.save(Board.create(noisy, "시끄러운 글", "본문"));
        noisyBoardId = noisyBoard.getBoardId();

        Board myBoard = boardRepository.save(Board.create(me, "내 글", "본문"));
        myBoardId = myBoard.getBoardId();

        noisyCommentId = boardCommentRepository.save(
                BoardComment.createRoot(myBoard, noisy, "시끄러운 댓글")).getBoardCommentId();

        entityManager.flush();
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    private List<Long> myBoardListIds() {
        return boardService.getBoardList(null, PAGE, memberBlockService.findBlockedIds(me.getMemberId()))
                .getContent().stream()
                .map(BoardInfo.BoardSummary::boardId)
                .toList();
    }

    private List<Long> commentIdsOnMyBoard() {
        return boardCommentService
                .findCommentsByBoardId(myBoardId, memberBlockService.findBlockedIds(me.getMemberId())).stream()
                .map(BoardInfo.Comment::commentId)
                .toList();
    }

    @Test
    @DisplayName("차단하면 글과 댓글이 사라지고, 해제하면 다시 보인다.")
    void blockHidesContentAndUnblockRestoresIt() {
        // Arrange - 차단 전에는 상대의 글과 댓글이 모두 보인다
        assertThat(myBoardListIds()).contains(noisyBoardId);
        assertThat(commentIdsOnMyBoard()).contains(noisyCommentId);

        // Act - 차단
        memberBlockService.block(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert - 목록과 댓글 양쪽에서 사라진다
        assertThat(myBoardListIds()).doesNotContain(noisyBoardId);
        assertThat(commentIdsOnMyBoard()).doesNotContain(noisyCommentId);
        assertThat(myBoardListIds()).contains(myBoardId);

        // Act - 해제
        memberBlockService.unblock(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert - 다시 보인다
        assertThat(myBoardListIds()).contains(noisyBoardId);
        assertThat(commentIdsOnMyBoard()).contains(noisyCommentId);
    }

    @Test
    @DisplayName("차단 목록에 상대가 닉네임과 함께 담기고, 해제하면 비워진다.")
    void blockListReflectsCurrentState() {
        // Arrange
        assertThat(memberBlockService.findMyBlocks(me.getMemberId())).isEmpty();

        // Act
        memberBlockService.block(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        List<MemberBlockInfo.Blocked> blocks = memberBlockService.findMyBlocks(me.getMemberId());
        assertThat(blocks).hasSize(1);
        assertThat(blocks.get(0).memberId()).isEqualTo(noisy.getMemberId());
        assertThat(blocks.get(0).nickname()).isEqualTo("lcNoisy");

        // Act
        memberBlockService.unblock(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(memberBlockService.findMyBlocks(me.getMemberId())).isEmpty();
    }

    @Test
    @DisplayName("차단하면 상대는 내 글에 댓글을 달 수 없고, 해제하면 다시 달 수 있다.")
    void blockStopsCommentingAndUnblockAllowsItAgain() {
        // Arrange - 차단 전에는 상대가 내 글에 댓글을 달 수 있다
        assertThatCode(() -> boardCommentService.create(myBoardId, noisy.getMemberId(), 0L, "댓글"))
                .doesNotThrowAnyException();

        // Act - 차단
        memberBlockService.block(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThatThrownBy(() -> boardCommentService.create(myBoardId, noisy.getMemberId(), 0L, "또 댓글"))
                .isInstanceOf(MemberBlockException.BlockedByAuthorException.class);

        // Act - 해제
        memberBlockService.unblock(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThatCode(() -> boardCommentService.create(myBoardId, noisy.getMemberId(), 0L, "해제 후 댓글"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("차단은 내 화면에만 적용된다. 다른 사람에게는 그대로 보인다.")
    void blockingIsPerViewer() {
        // Arrange
        memberBlockService.block(me.getMemberId(), noisy.getMemberId());
        entityManager.flush();
        entityManager.clear();

        // Act - 상대 본인이 목록을 보면 차단 목록이 비어 있다
        List<Long> seenByNoisy = boardService
                .getBoardList(null, PAGE, memberBlockService.findBlockedIds(noisy.getMemberId()))
                .getContent().stream()
                .map(BoardInfo.BoardSummary::boardId)
                .toList();

        // Assert
        assertThat(seenByNoisy).contains(noisyBoardId, myBoardId);
    }
}
