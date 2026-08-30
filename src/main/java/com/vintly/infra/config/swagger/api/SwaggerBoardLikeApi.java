package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.board.BoardLikeResponse;
import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "BoardLike", description = "게시글 좋아요 관련 API")
public interface SwaggerBoardLikeApi {

    @Operation(summary = "게시글 좋아요 등록", description = "게시글에 좋아요를 등록합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<BoardLikeResponse.BoardLike> like(@PathVariable Long boardId);

    @Operation(summary = "게시글 좋아요 취소", description = "게시글 좋아요를 취소합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<BoardLikeResponse.BoardLike> unlike(@PathVariable Long boardId);

    @Operation(summary = "게시글 좋아요 상태 조회", description = "게시글의 좋아요 여부와 수를 조회합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<BoardLikeResponse.BoardLike> getStatus(@PathVariable Long boardId);
}
