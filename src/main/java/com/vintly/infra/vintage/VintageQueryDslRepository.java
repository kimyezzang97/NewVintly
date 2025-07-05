package com.vintly.infra.vintage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.member.entity.QMember.member;
import static com.vintly.domain.vintage.entity.QVintage.vintage;
import static com.vintly.domain.vintagecomment.entity.QVintageComment.vintageComment;
import static com.vintly.domain.vintageimg.entity.QVintageImg.vintageImg;
import static com.vintly.domain.vintagelike.entity.QVintageLike.vintageLike;

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

    public VintageInfo.VintageDetail getVintage(Long vintageId, Long memberId) {
        // 1. 이미지 리스트
        List<String> imagePaths = queryFactory
                .select(vintageImg.imgPath)
                .from(vintageImg)
                .where(vintageImg.vintage.vintageId.eq(vintageId))
                .fetch();

        // 2. 좋아요 수
        Integer likeCount = queryFactory
                .select(vintageLike.count().intValue())
                .from(vintageLike)
                .where(vintageLike.vintage.vintageId.eq(vintageId))
                .fetchOne();

        // 3. 사용자의 좋아요 여부
        boolean liked = queryFactory
                .selectOne()
                .from(vintageLike)
                .where(
                        vintageLike.vintage.vintageId.eq(vintageId),
                        vintageLike.member.memberId.eq(memberId)
                )
                .fetchFirst() != null;

        // 4. 댓글 목록
        List<VintageInfo.Comment> comments = queryFactory
                .select(Projections.constructor(
                        VintageInfo.Comment.class,
                        vintageComment.vintageCommentId,
                        member.memberId,
                        member.nickname,
                        vintageComment.content,
                        vintageComment.createdAt
                ))
                .from(vintageComment)
                .join(vintageComment.member, member)
                .where(vintageComment.vintage.vintageId.eq(vintageId))
                .orderBy(vintageComment.createdAt.desc())
                .fetch();

        // 5. 매장 기본 정보
        Vintage baseVintage = queryFactory
                .selectFrom(vintage)
                .where(vintage.vintageId.eq(vintageId))
                .fetchOne();

        if (baseVintage == null) {
            throw new EntityNotFoundException("해당 빈티지 매장을 찾을 수 없습니다.");
        }

        return new VintageInfo.VintageDetail(
                baseVintage.getVintageId(),
                baseVintage.getName(),
                baseVintage.getState(),
                baseVintage.getDistrict(),
                baseVintage.getDetailAddr(),
                baseVintage.getLat(),
                baseVintage.getLon(),
                imagePaths,
                likeCount != null ? likeCount : 0,
                liked,
                comments
        );
    }
}
