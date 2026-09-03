package com.vintly.infra.vintagecomment;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintagecomment.entity.QVintageComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.vintly.domain.member.entity.QMember.member;
import static com.vintly.domain.vintagecomment.entity.QVintageComment.vintageComment;

@Repository
@RequiredArgsConstructor
public class VintageCommentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId, List<Long> blockedIds) {
        return queryFactory
                .select(Projections.constructor(
                        VintageInfo.Comment.class,
                        vintageComment.vintageCommentId,
                        vintageComment.parentCommentId,
                        member.memberId,
                        vintageComment.authorNickname,
                        vintageComment.content,
                        vintageComment.createdAt,
                        vintageComment.createdAt.ne(vintageComment.updatedAt)
                ))
                .from(vintageComment)
                .leftJoin(vintageComment.member, member)
                .where(vintageComment.vintage.vintageId.eq(vintageId), notBlocked(blockedIds))
                .orderBy(vintageComment.createdAt.desc())
                .fetch();
    }

    /**
     * 차단한 회원의 댓글과, 그 회원의 최상위 댓글에 달린 대댓글을 함께 제외한다 (설계 결정 7번).
     *
     * vintage_comment 는 작성자 탈퇴 시 member_id 가 실제로 null 이 된다. NOT IN 은 NULL 을 만나면
     * 결과가 NULL 이라 행이 걸러지므로, 차단과 무관한 탈퇴자 댓글이 사라지지 않도록 NULL 을
     * 명시적으로 통과시킨다.
     */
    private BooleanExpression notBlocked(List<Long> blockedIds) {
        if (CollectionUtils.isEmpty(blockedIds)) return null;

        BooleanExpression authorNotBlocked =
                member.memberId.isNull().or(member.memberId.notIn(blockedIds));

        // 서브쿼리는 바깥 쿼리와 별칭이 겹치면 안 되므로 별도 Q 인스턴스를 쓴다
        QVintageComment parent = new QVintageComment("parentComment");

        BooleanExpression parentNotBlocked = vintageComment.parentCommentId.eq(0L).or(
                vintageComment.parentCommentId.notIn(
                        JPAExpressions.select(parent.vintageCommentId)
                                .from(parent)
                                .where(parent.parentCommentId.eq(0L),
                                        parent.member.memberId.in(blockedIds))
                ));

        return authorNotBlocked.and(parentNotBlocked);
    }
}
