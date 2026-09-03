package com.vintly.domain.block.repo;

import com.vintly.domain.block.entity.MemberBlock;

import java.util.List;

public interface MemberBlockRepository {

    MemberBlock save(MemberBlock memberBlock);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    // 조회 필터가 쓰는 목록. 비어 있으면 호출부에서 조건을 붙이지 않아야 한다.
    List<Long> findBlockedIdsByBlockerId(Long blockerId);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    // 탈퇴 시 이 회원이 관련된 차단을 방향 상관없이 전부 삭제
    void deleteAllByMemberId(Long memberId);
}
