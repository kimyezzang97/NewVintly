package com.vintly.infra.board;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardCommentRepositoryImpl implements BoardCommentRepository {

    private final BoardCommentJpaRepository jpaRepository;
    private final BoardCommentQueryDslRepository dslRepository;

    @Override
    public BoardComment save(BoardComment boardComment) {
        return jpaRepository.save(boardComment);
    }

    @Override
    public Optional<BoardComment> findById(Long boardCommentId) {
        return jpaRepository.findById(boardCommentId);
    }

    @Override
    public List<BoardInfo.Comment> findCommentsByBoardId(Long boardId) {
        return dslRepository.findCommentsByBoardId(boardId);
    }

    @Override
    public void deleteById(Long boardCommentId) {
        jpaRepository.deleteById(boardCommentId);
    }

    @Override
    public void deleteAllByParentId(Long parentId) {
        jpaRepository.deleteAllByParentId(parentId);
    }

    @Override
    public void anonymizeMemberInComments(Member member, String deletedNickname) {
        jpaRepository.anonymizeMemberInComments(member, deletedNickname);
    }
}
