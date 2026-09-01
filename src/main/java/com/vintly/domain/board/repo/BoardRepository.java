package com.vintly.domain.board.repo;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository {

    Board save(Board board);

    Optional<Board> findById(Long boardId);

    Page<BoardInfo.BoardSummary> findBoardList(String keyword, Pageable pageable);

    Board getReferenceById(Long boardId);

    void incrementViewCount(Long boardId);

    void delete(Board board);

    // 탈퇴 회원의 게시글 작성자 닉네임 익명화 (게시글 자체는 보존)
    void anonymizeMemberInBoards(Member member, String deletedNickname);
}
