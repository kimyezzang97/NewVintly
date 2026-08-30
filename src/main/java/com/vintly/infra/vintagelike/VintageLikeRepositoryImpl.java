package com.vintly.infra.vintagelike;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagelike.entity.VintageLike;
import com.vintly.domain.vintagelike.repo.VintageLikeRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VintageLikeRepositoryImpl implements VintageLikeRepository {

    private final VintageLikeJpaRepository jpaRepository;
    private final VintageLikeQueryDslRepository dslRepository;

    public VintageLikeRepositoryImpl(VintageLikeJpaRepository jpaRepository, VintageLikeQueryDslRepository dslRepository) {
        this.jpaRepository = jpaRepository;
        this.dslRepository = dslRepository;
    }

    // 빈티지 매장의 좋아요 수 조회
    @Override
    public int countLikesByVintageId(Long vintageId) {
        return dslRepository.countLikesByVintageId(vintageId);
    }

    // 해당 사용자가 해당 빈티지 매장을 좋아요 했는지 여부 조회
    @Override
    public boolean existsLikeByVintageIdAndMemberId(Long vintageId, Long memberId) {
        return dslRepository.existsLikeByVintageIdAndMemberId(vintageId, memberId);
    }

    @Override
    public void deleteAllLikesByVintage(Vintage vintage) {
        jpaRepository.deleteAllByVintage(vintage);
    }

    @Override
    public void save(VintageLike vintageLike) {
        jpaRepository.save(vintageLike);
    }

    @Override
    public long deleteByVintageAndMember(Vintage vintage, Member member) {
        return jpaRepository.deleteByVintageAndMember(vintage, member);
    }
}
