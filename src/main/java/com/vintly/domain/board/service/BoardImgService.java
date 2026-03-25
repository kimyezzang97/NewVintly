package com.vintly.domain.board.service;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardImg;
import com.vintly.domain.board.repo.BoardImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardImgService {

    private final BoardImgRepository boardImgRepository;

    public List<BoardImg> saveImgList(List<String> imgUrls, Board board) {
        List<BoardImg> boardImgList = new ArrayList<>();
        for (int i = 0; i < imgUrls.size(); i++) {
            boardImgList.add(BoardImg.create(board, imgUrls.get(i), i + 1));
        }
        return boardImgRepository.saveAll(boardImgList);
    }

    public List<BoardInfo.BoardImgInfo> findImgListByBoardId(Long boardId) {
        return boardImgRepository.findImgListByBoardId(boardId);
    }

    public List<BoardImg> findImgEntityListByBoardId(Long boardId) {
        return boardImgRepository.findImgEntityListByBoardId(boardId);
    }

    public void deleteAll(List<BoardImg> list) {
        boardImgRepository.deleteAll(list);
    }
}
