package com.vintly.infra.board;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.board.repo.BoardLikeRepository;
import com.vintly.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardLikeRepositoryImpl implements BoardLikeRepository {

    private final BoardLikeJpaRepository jpaRepository;
    private final BoardLikeQueryDslRepository dslRepository;

    @Override
    public void save(BoardLike boardLike) {
        jpaRepository.save(boardLike);
    }

    @Override
    public int countLikesByBoardId(Long boardId) {
        return dslRepository.countLikesByBoardId(boardId);
    }

    @Override
    public boolean existsLikeByBoardIdAndMemberId(Long boardId, Long memberId) {
        return dslRepository.existsLikeByBoardIdAndMemberId(boardId, memberId);
    }

    @Override
    public long deleteByBoardAndMember(Board board, Member member) {
        return jpaRepository.deleteByBoardAndMember(board, member);
    }

    @Override
    public void deleteAllLikesByBoard(Board board) {
        jpaRepository.deleteAllByBoard(board);
    }
}
