package com.vintly.infra.vintagecomment;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.member.entity.QMember.member;
import static com.vintly.domain.vintagecomment.entity.QVintageComment.vintageComment;

@Repository
@RequiredArgsConstructor
public class VintageCommentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId) {
        return queryFactory
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
    }
}
