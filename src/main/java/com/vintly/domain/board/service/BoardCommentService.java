package com.vintly.domain.board.service;

import com.vintly.interfaces.member.MemberException;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.repo.MemberRepository;
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

    @Transactional(readOnly = true)
    public List<BoardInfo.Comment> findCommentsByBoardId(Long boardId, List<Long> blockedIds) {
        return boardCommentRepository.findCommentsByBoardId(boardId, blockedIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long boardId, Long memberId, Long parentId, String content) {
        Board boardRef = boardRepository.getReferenceById(boardId);
        var memberRef = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

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

            comment = BoardComment.createReply(boardRef, memberRef, parentId, content);
        }

        return boardCommentRepository.save(comment).getBoardCommentId();
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
