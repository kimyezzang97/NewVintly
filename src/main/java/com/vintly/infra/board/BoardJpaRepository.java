package com.vintly.infra.board;

import com.vintly.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {

    @Modifying
    @Query("UPDATE board b SET b.viewCount = b.viewCount + 1 WHERE b.boardId = :boardId")
    void incrementViewCount(@Param("boardId") Long boardId);
}
