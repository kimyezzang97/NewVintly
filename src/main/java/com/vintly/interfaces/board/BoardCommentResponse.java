package com.vintly.interfaces.board;

import io.swagger.v3.oas.annotations.media.Schema;

public class BoardCommentResponse {

    public record Create(
            @Schema(description = "생성된 댓글 ID") Long commentId,
            @Schema(description = "부모 댓글 ID (최상위면 0)") Long parentId
    ) {}
}
