package com.vintly.infra.block;

import com.vintly.domain.block.entity.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberBlockJpaRepository extends JpaRepository<MemberBlock, Long> {

    boolean existsByBlockerMemberIdAndBlockedMemberId(Long blockerId, Long blockedId);

    // 차단 목록은 상대 닉네임을 함께 쓰므로 fetch join 으로 N+1 을 막는다
    @Query("SELECT mb FROM member_block mb JOIN FETCH mb.blocked WHERE mb.blocker.memberId = :blockerId ORDER BY mb.blockId DESC")
    List<MemberBlock> findAllByBlockerId(@Param("blockerId") Long blockerId);

    @Query("SELECT mb.blocked.memberId FROM member_block mb WHERE mb.blocker.memberId = :blockerId")
    List<Long> findBlockedIdsByBlockerId(@Param("blockerId") Long blockerId);

    // 파생 쿼리(select 후 건별 delete) 대신 단일 delete 문으로 처리
    @Modifying
    @Query("DELETE FROM member_block mb WHERE mb.blocker.memberId = :blockerId AND mb.blocked.memberId = :blockedId")
    void deleteByBlockerIdAndBlockedId(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Modifying
    @Query("DELETE FROM member_block mb WHERE mb.blocker.memberId = :memberId OR mb.blocked.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
