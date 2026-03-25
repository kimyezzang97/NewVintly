package com.vintly.infra.board;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.DelStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
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

    public Page<BoardInfo.BoardSummary> findBoardList(String keyword, Pageable pageable) {
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
                .leftJoin(boardComment).on(
                        boardComment.board.boardId.eq(board.boardId)
                                .and(boardComment.delStatus.eq(DelStatus.N))
                )
                .leftJoin(boardImg).on(
                        boardImg.board.boardId.eq(board.boardId)
                                .and(boardImg.sortOrder.eq(1))
                )
                .where(
                        board.delStatus.eq(DelStatus.N),
                        containsKeyword(keyword)
                )
                .groupBy(board.boardId, board.createdAt, board.updatedAt)
                .orderBy(board.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(board.count())
                .from(board)
                .where(
                        board.delStatus.eq(DelStatus.N),
                        containsKeyword(keyword)
                );

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
}
