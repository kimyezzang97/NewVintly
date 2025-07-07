package com.vintly.infra.vintageimg;

import com.querydsl.jpa.impl.JPAQueryFactory;
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
}
