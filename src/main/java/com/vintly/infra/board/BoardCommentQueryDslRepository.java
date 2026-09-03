package com.vintly.infra.board;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.QBoardComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.vintly.domain.board.entity.QBoardComment.boardComment;
import static com.vintly.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BoardCommentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<BoardInfo.Comment> findCommentsByBoardId(Long boardId, List<Long> blockedIds) {
        return queryFactory
                .select(Projections.constructor(
                        BoardInfo.Comment.class,
                        boardComment.boardCommentId,
                        boardComment.parentId,
                        member.memberId,
                        boardComment.authorNickname,
                        boardComment.content,
                        boardComment.createdAt,
                        boardComment.createdAt.ne(boardComment.updatedAt)
                ))
                .from(boardComment)
                .leftJoin(boardComment.member, member)
                .where(boardComment.board.boardId.eq(boardId), notBlocked(blockedIds))
                .orderBy(boardComment.createdAt.desc())
                .fetch();
    }

    /**
     * 차단한 회원의 댓글과, 그 회원의 최상위 댓글에 달린 대댓글을 함께 제외한다 (설계 결정 7번).
     *
     * 두 가지에 주의한다.
     *
     * 1. NULL 을 명시적으로 통과시킨다. 작성자가 탈퇴하면 member_id 는 orphan 값으로 남는데
     *    leftJoin 이 짝을 찾지 못해 member.memberId 가 NULL 이 된다. NOT IN 은 NULL 을 만나면
     *    결과가 NULL 이라 행이 걸러지므로, 차단과 무관한 탈퇴자 댓글이 통째로 사라진다.
     *
     * 2. 대댓글은 작성자만 봐서는 판별되지 않는다. 부모가 차단 대상인지 확인해야 하므로
     *    최상위 댓글(parentId = 0) 중 차단 대상이 쓴 것의 ID 를 서브쿼리로 구한다.
     */
    private BooleanExpression notBlocked(List<Long> blockedIds) {
        if (CollectionUtils.isEmpty(blockedIds)) return null;

        BooleanExpression authorNotBlocked =
                member.memberId.isNull().or(member.memberId.notIn(blockedIds));

        // 서브쿼리는 바깥 쿼리와 별칭이 겹치면 안 되므로 별도 Q 인스턴스를 쓴다
        QBoardComment parent = new QBoardComment("parentComment");

        BooleanExpression parentNotBlocked = boardComment.parentId.eq(0L).or(
                boardComment.parentId.notIn(
                        JPAExpressions.select(parent.boardCommentId)
                                .from(parent)
                                .where(parent.parentId.eq(0L),
                                        parent.member.memberId.in(blockedIds))
                ));

        return authorNotBlocked.and(parentNotBlocked);
    }
}
