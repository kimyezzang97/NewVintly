package com.vintly.interfaces.board;

import com.vintly.application.board.BoardFacade;
import com.vintly.infra.config.swagger.api.SwaggerBoardApi;
import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.presentation.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards")
@Validated
@RequiredArgsConstructor
public class BoardController implements SwaggerBoardApi {

    private final BoardFacade boardFacade;

    // 게시글 목록 조회 (검색)
    @GetMapping
    public ApiResponse<?> getBoardList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return new ApiResponse<>(true, 200, "", PageResponse.from(boardFacade.getBoardList(keyword, pageable)));
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<?> getBoard(@PathVariable Long id) {
        return new ApiResponse<>(true, 200, "", boardFacade.getBoard(id));
    }

    // 게시글 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createBoard(@ModelAttribute BoardRequest.CreateBoard request) {
        boardFacade.createBoard(request);
        return new ApiResponse<>(true, 200, "게시글이 등록되었습니다.", null);
    }

    // 게시글 수정
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> updateBoard(@PathVariable Long id, @ModelAttribute BoardRequest.UpdateBoard request) {
        boardFacade.updateBoard(id, request);
        return new ApiResponse<>(true, 200, "게시글이 수정되었습니다.", null);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteBoard(@PathVariable Long id) {
        boardFacade.deleteBoard(id);
        return new ApiResponse<>(true, 200, "게시글이 삭제되었습니다.", null);
    }
}
