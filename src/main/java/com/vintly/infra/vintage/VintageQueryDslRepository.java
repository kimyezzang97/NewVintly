package com.vintly.infra.vintage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.vintage.entity.QVintage.vintage;
import static com.vintly.domain.vintageimg.entity.QVintageImg.vintageImg;

@Repository
@RequiredArgsConstructor
public class VintageQueryDslRepository {

    private final JPAQueryFactory queryFactory;

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
}
