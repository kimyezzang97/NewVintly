package com.vintly.domain.board.service;

import com.vintly.interfaces.member.MemberException;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.block.service.MemberBlockService;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.interfaces.block.MemberBlockException;
import com.vintly.interfaces.board.BoardCommentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final MemberBlockService memberBlockService;

    @Transactional(readOnly = true)
    public List<BoardInfo.Comment> findCommentsByBoardId(Long boardId, List<Long> blockedIds) {
        return boardCommentRepository.findCommentsByBoardId(boardId, blockedIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long boardId, Long memberId, Long parentId, String content) {
        Board boardRef = boardRepository.getReferenceById(boardId);
        var memberRef = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        // 글쓴이가 나를 차단했으면 댓글을 달 수 없다 (설계 결정 6번)
        rejectIfBlockedBy(memberId, boardRef.getMember());

        BoardComment comment;

        if (parentId == null || parentId == 0) {
            comment = BoardComment.createRoot(boardRef, memberRef, content);
        } else {
            BoardComment parent = boardCommentRepository.findById(parentId)
                    .orElseThrow(BoardCommentException.ParentCommentNotFoundException::new);

            if (!parent.getBoard().getBoardId().equals(boardId)) {
                throw new BoardCommentException.ParentCommentMismatchException();
            }

            if (parent.getParentId() != 0L) {
                throw new BoardCommentException.ReplyDepthExceededException();
            }

            // 부모 댓글 작성자가 나를 차단한 경우도 막는다. 차단은 단방향 표시라 상대의 댓글은
            // 내게 그대로 보이므로, 이 검사가 없으면 답글로 계속 접근할 수 있다.
            rejectIfBlockedBy(memberId, parent.getMember());

            comment = BoardComment.createReply(boardRef, memberRef, parentId, content);
        }

        return boardCommentRepository.save(comment).getBoardCommentId();
    }

    /**
     * 상대가 나를 차단했으면 예외를 던진다.
     *
     * 방향에 주의할 것. 막아야 하는 것은 "내가 차단한 사람"이 아니라 "나를 차단한 사람"이다.
     * 작성자가 이미 탈퇴했으면(null) 차단 관계가 남아 있을 수 없으므로 통과시킨다.
     */
    private void rejectIfBlockedBy(Long myId, Member other) {
        if (other == null || other.getMemberId() == null) return;
        if (memberBlockService.isBlockedBy(myId, other.getMemberId())) {
            throw new MemberBlockException.BlockedByAuthorException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long commentId, Long memberId, String content) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(BoardCommentException.CommentNotFoundException::new);

        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new BoardCommentException.CommentNotOwnedException();
        }

        comment.updateContent(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAllByBoardId(Long boardId) {
        boardCommentRepository.deleteAllByBoardId(boardId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId, Long memberId) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(BoardCommentException.CommentNotFoundException::new);

        if (!comment.getMember().getMemberId().equals(memberId)) {
            throw new BoardCommentException.CommentNotOwnedException();
        }

        // 최상위 댓글이면 대댓글도 함께 삭제
        if (comment.getParentId() == 0L) {
            boardCommentRepository.deleteAllByParentId(commentId);
        }

        boardCommentRepository.deleteById(commentId);
    }
}
