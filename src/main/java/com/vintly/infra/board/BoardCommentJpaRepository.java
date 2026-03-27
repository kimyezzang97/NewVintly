package com.vintly.infra.board;

import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentJpaRepository extends JpaRepository<BoardComment, Long> {

    void deleteAllByParentId(Long parentId);

    // member_id는 NOT NULL이므로 member를 null로 설정하지 않고 닉네임만 익명화
    @Modifying
    @Query("UPDATE board_comment bc SET bc.authorNickname = :deletedNickname WHERE bc.member = :member")
    void anonymizeMemberInComments(@Param("member") Member member, @Param("deletedNickname") String deletedNickname);
}
