package com.vintly.infra.board;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.vintly.domain.board.entity.QBoard.board;
import static com.vintly.domain.board.entity.QBoardImg.boardImg;
import static com.vintly.domain.board.entity.QBoardLike.boardLike;
import static com.vintly.domain.board.entity.QBoardComment.boardComment;

@Repository
@RequiredArgsConstructor
public class BoardQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<BoardInfo.BoardSummary> findBoardList(String keyword, Pageable pageable, List<Long> blockedIds) {
        List<BoardInfo.BoardSummary> content = queryFactory
                .select(Projections.constructor(
                        BoardInfo.BoardSummary.class,
                        board.boardId,
                        board.member.memberId,
                        board.authorNickname,
                        board.title,
                        board.viewCount,
                        boardLike.boardLikeId.count(),
                        boardComment.boardCommentId.countDistinct(),
                        boardImg.imgPath.min(),
                        board.createdAt,
                        board.updatedAt
                ))
                .from(board)
                .leftJoin(boardLike).on(boardLike.board.boardId.eq(board.boardId))
                .leftJoin(boardComment).on(boardComment.board.boardId.eq(board.boardId))
                .leftJoin(boardImg).on(
                        boardImg.board.boardId.eq(board.boardId)
                                .and(boardImg.sortOrder.eq(1))
                )
                .where(containsKeyword(keyword), notBlockedAuthor(blockedIds))
                .groupBy(board.boardId, board.createdAt, board.updatedAt)
                .orderBy(board.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(board.count())
                .from(board)
                .where(containsKeyword(keyword), notBlockedAuthor(blockedIds));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Optional<Board> findById(Long boardId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(board)
                        .where(board.boardId.eq(boardId))
                        .fetchOne()
        );
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return board.title.containsIgnoreCase(keyword)
                .or(board.content.containsIgnoreCase(keyword));
    }

    /**
     * 차단한 회원의 글을 제외한다.
     *
     * 차단이 없으면 null 을 돌려 조건을 붙이지 않는다. QueryDSL 은 빈 notIn 도 안전하게 처리하므로
     * 이 가드가 없어도 동작하지만, 쓸모없는 조건을 SQL 에 싣지 않고 의도를 드러내기 위해 둔다.
     *
     * board.member_id 는 NOT NULL 이고 탈퇴해도 orphan 값이 남으므로 여기서는 NULL 을 따로 다루지
     * 않는다. leftJoin 으로 회원을 붙여 조회하는 댓글 쪽은 사정이 다르니 그쪽 조건과 혼동하지 말 것.
     */
    private BooleanExpression notBlockedAuthor(List<Long> blockedIds) {
        if (CollectionUtils.isEmpty(blockedIds)) return null;
        return board.member.memberId.notIn(blockedIds);
    }
}
