package com.vintly.domain.board.service;

import com.vintly.interfaces.member.MemberException;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.board.repo.BoardLikeRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.board.BoardLikeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardLikeRepository boardLikeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional(rollbackFor = Exception.class)
    public BoardLikeResponse.BoardLike like(Long boardId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        if (!boardLikeRepository.existsLikeByBoardIdAndMemberId(boardId, memberId)) {
            Board board = boardRepository.getReferenceById(boardId);
            var member = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

            try {
                boardLikeRepository.save(BoardLike.create(board, member));
            } catch (DataIntegrityViolationException e) {
                log.error(e.getMessage());
                throw new DataIntegrityViolationException(e.getMessage());
            }
        }

        long count = boardLikeRepository.countLikesByBoardId(boardId);
        return new BoardLikeResponse.BoardLike(true, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public BoardLikeResponse.BoardLike unlike(Long boardId) {
        Board board = boardRepository.getReferenceById(boardId);
        var member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(MemberException.MemberNotFoundException::new);

        boardLikeRepository.deleteByBoardAndMember(board, member);

        long count = boardLikeRepository.countLikesByBoardId(boardId);
        return new BoardLikeResponse.BoardLike(false, count);
    }

    @Transactional(readOnly = true)
    public BoardLikeResponse.BoardLike getStatus(Long boardId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        boolean liked = boardLikeRepository.existsLikeByBoardIdAndMemberId(boardId, memberId);
        long count = boardLikeRepository.countLikesByBoardId(boardId);
        return new BoardLikeResponse.BoardLike(liked, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAllLikesByBoard(Board board) {
        boardLikeRepository.deleteAllLikesByBoard(board);
    }
}
