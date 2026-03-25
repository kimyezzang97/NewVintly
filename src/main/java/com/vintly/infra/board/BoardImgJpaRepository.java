package com.vintly.infra.board;

import com.vintly.domain.board.entity.BoardImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardImgJpaRepository extends JpaRepository<BoardImg, Long> {
    List<BoardImg> findByBoard_BoardIdOrderBySortOrderAsc(Long boardId);
}
