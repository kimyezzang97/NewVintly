package com.vintly.infra.block;

import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.block.repo.MemberBlockRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberBlockRepositoryImpl implements MemberBlockRepository {

    private final MemberBlockJpaRepository jpaRepository;

    public MemberBlockRepositoryImpl(MemberBlockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * 차단 저장.
     *
     * 신고와 같은 이유로 saveAndFlush다. 중복 차단은 서비스가 먼저 걸러내지만 동시 요청이
     * 양쪽 다 통과하는 창이 남고, 지연 플러시로 두면 유니크 제약 위반이 커밋 시점에 터져
     * 서비스가 잡을 수 없다.
     */
    @Override
    public MemberBlock save(MemberBlock memberBlock) {
        return jpaRepository.saveAndFlush(memberBlock);
    }

    @Override
    public boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return jpaRepository.existsByBlockerMemberIdAndBlockedMemberId(blockerId, blockedId);
    }

    @Override
    public List<MemberBlock> findAllByBlockerId(Long blockerId) {
        return jpaRepository.findAllByBlockerId(blockerId);
    }

    @Override
    public List<Long> findBlockedIdsByBlockerId(Long blockerId) {
        return jpaRepository.findBlockedIdsByBlockerId(blockerId);
    }

    @Override
    public void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        jpaRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        jpaRepository.deleteAllByMemberId(memberId);
    }
}
