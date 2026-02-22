package com.vintly.interfaces.vintageComment;

import io.swagger.v3.oas.annotations.media.Schema;

public class VintageCommentResponse {

    public record Create(
            @Schema(description = "생성된 댓글 ID", example = "1")
            Long commentId,

            @Schema(description = "상위 댓글 ID (0이면 최상위 댓글)", example = "0")
            Long parentCommentId
    ) {}
}
