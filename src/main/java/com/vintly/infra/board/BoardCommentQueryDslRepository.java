package com.vintly.infra.board;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.board.dto.BoardInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.board.entity.QBoardComment.boardComment;
import static com.vintly.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BoardCommentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public List<BoardInfo.Comment> findCommentsByBoardId(Long boardId) {
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
                .where(boardComment.board.boardId.eq(boardId))
                .orderBy(boardComment.createdAt.desc())
                .fetch();
    }
}
