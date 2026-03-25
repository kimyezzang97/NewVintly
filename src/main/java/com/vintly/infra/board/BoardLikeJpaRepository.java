package com.vintly.infra.board;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardLikeJpaRepository extends JpaRepository<BoardLike, Long> {

    void deleteAllByBoard(Board board);

    long deleteByBoardAndMember(Board board, Member member);
}
