package com.vintly.infra.vintageimg;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintageimg.entity.VintageImg;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.vintageimg.entity.QVintageImg.vintageImg;

@Repository
@RequiredArgsConstructor
public class VintageImgQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    // 빈티지 매장의 전체 이미지 경로 리스트 조회
    public List<String> findImagePathListByVintageId(Long vintageId) {
        return queryFactory
                .select(vintageImg.imgPath)
                .from(vintageImg)
                .where(vintageImg.vintage.vintageId.eq(vintageId))
                .fetch();
    }

    // 해당 빈티지 매장의 (이미지 경로, 이미지 id) 리스트 조회
    public List<VintageInfo.Image> findImgListByVintageId(Long vintageId) {
        return queryFactory
                .select(Projections.constructor(
                                VintageInfo.Image.class,
                        vintageImg.vintageImgId, vintageImg.imgPath )
                )
                .from(vintageImg)
                .where(vintageImg.vintage.vintageId.eq(vintageId))
                .fetch();
    }
}
