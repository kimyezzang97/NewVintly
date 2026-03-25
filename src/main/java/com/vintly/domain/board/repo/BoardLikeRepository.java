package com.vintly.domain.board.repo;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.member.entity.Member;

public interface BoardLikeRepository {

    void save(BoardLike boardLike);

    int countLikesByBoardId(Long boardId);

    boolean existsLikeByBoardIdAndMemberId(Long boardId, Long memberId);

    long deleteByBoardAndMember(Board board, Member member);

    void deleteAllLikesByBoard(Board board);
}
