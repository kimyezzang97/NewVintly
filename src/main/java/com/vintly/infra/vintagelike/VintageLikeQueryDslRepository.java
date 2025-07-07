package com.vintly.infra.vintagelike;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.vintly.domain.vintagelike.entity.QVintageLike.vintageLike;

@Repository
@RequiredArgsConstructor
public class VintageLikeQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    // 빈티지 매장의 좋아요 수 조회 (null 방지)
    public int countLikesByVintageId(Long vintageId) {
        Integer count = queryFactory
                .select(vintageLike.count().intValue())
                .from(vintageLike)
                .where(vintageLike.vintage.vintageId.eq(vintageId))
                .fetchOne();

        return count != null ? count : 0;
    }

    // 해당 사용자가 해당 빈티지 매장을 좋아요 했는지 여부 조회
    public boolean existsLikeByVintageIdAndMemberId(Long vintageId, Long memberId) {
        Integer result = queryFactory
                .selectOne()
                .from(vintageLike)
                .where(
                        vintageLike.vintage.vintageId.eq(vintageId),
                        vintageLike.member.memberId.eq(memberId)
                )
                .fetchFirst();

        return result != null;
    }
}
