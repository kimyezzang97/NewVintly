package com.vintly.infra.board;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.BoardImg;
import com.vintly.domain.board.repo.BoardImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vintly.domain.board.entity.QBoardImg.boardImg;

@Repository
@RequiredArgsConstructor
public class BoardImgRepositoryImpl implements BoardImgRepository {

    private final BoardImgJpaRepository boardImgJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardImg> saveAll(List<BoardImg> boardImgList) {
        return boardImgJpaRepository.saveAll(boardImgList);
    }

    @Override
    public List<BoardInfo.BoardImgInfo> findImgListByBoardId(Long boardId) {
        return queryFactory
                .select(Projections.constructor(
                        BoardInfo.BoardImgInfo.class,
                        boardImg.boardImgId,
                        boardImg.imgPath,
                        boardImg.sortOrder
                ))
                .from(boardImg)
                .where(boardImg.board.boardId.eq(boardId))
                .orderBy(boardImg.sortOrder.asc())
                .fetch();
    }

    @Override
    public List<BoardImg> findImgEntityListByBoardId(Long boardId) {
        return boardImgJpaRepository.findByBoard_BoardIdOrderBySortOrderAsc(boardId);
    }

    @Override
    public void deleteAll(List<BoardImg> list) {
        boardImgJpaRepository.deleteAll(list);
    }
}
