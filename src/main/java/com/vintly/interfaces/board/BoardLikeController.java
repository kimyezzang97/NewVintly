package com.vintly.interfaces.board;

import com.vintly.domain.board.service.BoardLikeService;
import com.vintly.infra.config.swagger.api.SwaggerBoardLikeApi;
import com.vintly.interfaces.presentation.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/likes")
@RequiredArgsConstructor
public class BoardLikeController implements SwaggerBoardLikeApi {

    private final BoardLikeService boardLikeService;

    @PostMapping
    public ApiResponse<BoardLikeResponse.BoardLike> like(@PathVariable Long boardId) {
        return new ApiResponse<>(true, 200, "", boardLikeService.like(boardId));
    }

    @DeleteMapping
    public ApiResponse<BoardLikeResponse.BoardLike> unlike(@PathVariable Long boardId) {
        return new ApiResponse<>(true, 200, "", boardLikeService.unlike(boardId));
    }

    @GetMapping
    public ApiResponse<BoardLikeResponse.BoardLike> getStatus(@PathVariable Long boardId) {
        return new ApiResponse<>(true, 200, "", boardLikeService.getStatus(boardId));
    }
}
