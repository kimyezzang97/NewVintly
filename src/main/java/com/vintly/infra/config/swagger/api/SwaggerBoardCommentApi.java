package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.board.BoardCommentRequest;
import com.vintly.interfaces.board.BoardCommentResponse;
import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "BoardComment", description = "게시글 댓글 관련 API")
public interface SwaggerBoardCommentApi {

    @Operation(summary = "게시글 댓글 등록", description = "게시글에 댓글 또는 대댓글을 등록합니다. parentId가 없거나 0이면 최상위 댓글입니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<BoardCommentResponse.Create> createComment(@PathVariable Long boardId, @Valid @RequestBody BoardCommentRequest.Create req);

    @Operation(summary = "게시글 댓글 수정", description = "게시글 댓글을 수정합니다. 본인 댓글만 수정 가능합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> updateComment(@PathVariable Long boardId, @PathVariable Long commentId, @Valid @RequestBody BoardCommentRequest.Update req);

    @Operation(summary = "게시글 댓글 삭제", description = "게시글 댓글을 삭제합니다. 본인 댓글만 삭제 가능합니다. 최상위 댓글 삭제 시 대댓글도 함께 삭제됩니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId);
}
