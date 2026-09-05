package com.vintly.domain.board.repo;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository {

    BoardComment save(BoardComment boardComment);

    Optional<BoardComment> findById(Long boardCommentId);

    List<BoardInfo.Comment> findCommentsByBoardId(Long boardId, List<Long> blockedIds);

    void deleteById(Long boardCommentId);

    void deleteAllByParentId(Long parentId);

    void deleteAllByBoardId(Long boardId);

    void anonymizeMemberInComments(Member member, String deletedNickname);
}
