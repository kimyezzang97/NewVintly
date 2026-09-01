package com.vintly.infra.board;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardLikeJpaRepository extends JpaRepository<BoardLike, Long> {

    void deleteAllByBoard(Board board);

    long deleteByBoardAndMember(Board board, Member member);

    // 파생 쿼리(select 후 건별 delete) 대신 단일 delete 문으로 처리
    @Modifying
    @Query("DELETE FROM board_like bl WHERE bl.member = :member")
    void deleteAllByMember(@Param("member") Member member);
}
