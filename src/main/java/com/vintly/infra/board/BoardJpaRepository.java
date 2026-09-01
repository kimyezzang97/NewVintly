package com.vintly.infra.board;

import com.vintly.domain.board.entity.Board;
import com.vintly.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardJpaRepository extends JpaRepository<Board, Long> {

    // 조회수 증가는 '수정'이 아니므로 updated_at을 자기 값으로 대입해 ON UPDATE CURRENT_TIMESTAMP 자동 갱신을 막는다
    @Modifying
    @Query("UPDATE board b SET b.viewCount = b.viewCount + 1, b.updatedAt = b.updatedAt WHERE b.boardId = :boardId")
    void incrementViewCount(@Param("boardId") Long boardId);

    // member_id는 NOT NULL이므로 member는 orphan으로 두고 닉네임만 익명화.
    // updated_at은 ON UPDATE CURRENT_TIMESTAMP라 자기 값을 그대로 대입해 자동 갱신을 막는다 (익명화는 '수정'이 아니므로)
    @Modifying
    @Query("UPDATE board b SET b.authorNickname = :deletedNickname, b.updatedAt = b.updatedAt WHERE b.member = :member")
    void anonymizeMemberInBoards(@Param("member") Member member, @Param("deletedNickname") String deletedNickname);
}
