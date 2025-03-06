package com.vintly.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("boards")
public class BoardController {

    private final BoardRepository boardRepository;

    public BoardController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @GetMapping()
    public List<Board> getBoards(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Board> pageOfBoards = boardRepository.findAllByOrderByCreatedAtDesc(pageable);
        return pageOfBoards.getContent();
    }
}
