package com.vintly.domain.board.repo;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.BoardImg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardImgRepository {

    List<BoardImg> saveAll(List<BoardImg> boardImgList);

    List<BoardInfo.BoardImgInfo> findImgListByBoardId(Long boardId);

    List<BoardImg> findImgEntityListByBoardId(Long boardId);

    void deleteAll(List<BoardImg> list);
}
