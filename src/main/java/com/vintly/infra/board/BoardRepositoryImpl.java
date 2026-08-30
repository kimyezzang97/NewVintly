package com.vintly.infra.board;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.repo.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardJpaRepository boardJpaRepository;
    private final BoardQueryDslRepository boardQueryDslRepository;

    @Override
    public Board save(Board board) {
        return boardJpaRepository.save(board);
    }

    @Override
    public Optional<Board> findById(Long boardId) {
        return boardQueryDslRepository.findById(boardId);
    }

    @Override
    public Page<BoardInfo.BoardSummary> findBoardList(String keyword, Pageable pageable) {
        return boardQueryDslRepository.findBoardList(keyword, pageable);
    }

    @Override
    public Board getReferenceById(Long boardId) {
        return boardJpaRepository.getReferenceById(boardId);
    }

    @Override
    public void incrementViewCount(Long boardId) {
        boardJpaRepository.incrementViewCount(boardId);
    }

    @Override
    public void delete(Board board) {
        boardJpaRepository.delete(board);
    }
}
