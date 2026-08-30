package com.vintly.infra.board;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.vintly.domain.board.entity.QBoardLike.boardLike;

@Repository
@RequiredArgsConstructor
public class BoardLikeQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public int countLikesByBoardId(Long boardId) {
        Integer count = queryFactory
                .select(boardLike.count().intValue())
                .from(boardLike)
                .where(boardLike.board.boardId.eq(boardId))
                .fetchOne();

        return count != null ? count : 0;
    }

    public boolean existsLikeByBoardIdAndMemberId(Long boardId, Long memberId) {
        Integer result = queryFactory
                .selectOne()
                .from(boardLike)
                .where(
                        boardLike.board.boardId.eq(boardId),
                        boardLike.member.memberId.eq(memberId)
                )
                .fetchFirst();

        return result != null;
    }
}
