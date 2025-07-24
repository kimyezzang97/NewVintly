package com.vintly.infra.vintage;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.vintly.domain.member.entity.QMember.member;
import static com.vintly.domain.vintage.entity.QVintage.vintage;
import static com.vintly.domain.vintagecomment.entity.QVintageComment.vintageComment;
import static com.vintly.domain.vintageimg.entity.QVintageImg.vintageImg;
import static com.vintly.domain.vintagelike.entity.QVintageLike.vintageLike;

@Repository
@RequiredArgsConstructor
public class VintageQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    // 빈티지 매장 리스트 조회
    public List<VintageInfo.Vintage> getAllVintageList() {
        return queryFactory
                .select(Projections.constructor(
                        VintageInfo.Vintage.class,
                        vintage.vintageId,
                        vintage.name,
                        vintage.state,
                        vintage.district,
                        vintage.detailAddr,
                        vintage.lat,
                        vintage.lon,
                        vintageImg.imgPath
                ))
                .from(vintage)
                .leftJoin(vintageImg)
                .on(
                        vintageImg.vintage.vintageId.eq(vintage.vintageId)
                                .and(vintageImg.vintageImgId.eq(vintage.vintageImageId))
                )
                .fetch();
    }

    // 빈티지 매장 기본 정보 (단일 엔티티) 조회
    public Optional<Vintage> findBasicInfoById(Long vintageId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(vintage)
                        .where(vintage.vintageId.eq(vintageId))
                        .fetchOne()
        );
    }

    public List<VintageInfo.Vintage> findByLocation(String state, String district){
        return queryFactory
                .selectFrom(vintage)
                .where(
                        eqState(state),
                        eqDistrict(district)
                )
                .fetch();
    }

    private BooleanExpression eqState(String state) {
        return StringUtils.hasText(state) ? vintage.state.eq(state) : null;
    }

    private BooleanExpression eqDistrict(String district) {
        return StringUtils.hasText(district) ? vintage.district.eq(district) : null;
    }

}
