package com.vintly.domain.vintagelike.repo;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintageimg.entity.VintageImg;
import com.vintly.domain.vintagelike.entity.VintageLike;

public interface VintageLikeRepository {

    // 해당 빈티지 매장의 좋아요 수 카운트
    int countLikesByVintageId(Long vintageId);

    // 특정 유저가 해당 매장에 좋아요를 눌렀는지 여부 확인
    boolean existsLikeByVintageIdAndMemberId(Long vintageId, Long memberId);

    void deleteAllLikesByVintage(Vintage vintage);

    void save(VintageLike vintageLike);

    long deleteByVintageAndMember(Vintage vintage, Member member);
}
