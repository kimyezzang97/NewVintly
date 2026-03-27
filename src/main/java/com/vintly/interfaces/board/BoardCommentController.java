package com.vintly.interfaces.board;

import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.infra.config.swagger.api.SwaggerBoardCommentApi;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.presentation.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/comments")
@RequiredArgsConstructor
@Validated
public class BoardCommentController implements SwaggerBoardCommentApi {

    private final BoardCommentService boardCommentService;

    @PostMapping
    public ApiResponse<BoardCommentResponse.Create> createComment(@PathVariable Long boardId,
                                                                  @Valid @RequestBody BoardCommentRequest.Create req) {
        Long commentId = boardCommentService.create(boardId, SecurityUtil.getCurrentMemberId(), req.parentId(), req.comment());
        Long parentId = req.parentId() != null ? req.parentId() : 0L;
        return new ApiResponse<>(true, 200, "", new BoardCommentResponse.Create(commentId, parentId));
    }

    @PutMapping("/{commentId}")
    public ApiResponse<?> updateComment(@PathVariable Long boardId,
                                        @PathVariable Long commentId,
                                        @Valid @RequestBody BoardCommentRequest.Update req) {
        boardCommentService.update(commentId, SecurityUtil.getCurrentMemberId(), req.comment());
        return new ApiResponse<>(true, 200, "댓글이 수정되었습니다.", null);
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<?> deleteComment(@PathVariable Long boardId,
                                        @PathVariable Long commentId) {
        boardCommentService.delete(commentId, SecurityUtil.getCurrentMemberId());
        return new ApiResponse<>(true, 200, "댓글이 삭제되었습니다.", null);
    }
}
