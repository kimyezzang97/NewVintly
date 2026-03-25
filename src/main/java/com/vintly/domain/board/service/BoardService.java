package com.vintly.domain.board.service;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.entity.Member;
import com.vintly.interfaces.board.BoardException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public Page<BoardInfo.BoardSummary> getBoardList(String keyword, Pageable pageable) {
        return boardRepository.findBoardList(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Board> findById(Long boardId) {
        return boardRepository.findById(boardId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Board createBoard(Member member, String title, String content) {
        return boardRepository.save(Board.create(member, title, content));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(Board board, String title, String content) {
        board.updateInfo(title, content);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByWriter(Board board, Member member) {
        if (board.getMember() == null || !board.getMember().getMemberId().equals(member.getMemberId())) {
            throw new BoardException.BoardForbiddenException();
        }
        board.deleteByWriter();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByAdmin(Board board) {
        board.deleteByAdmin();
    }

    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Board board) {
        board.incrementViewCount();
    }
}
