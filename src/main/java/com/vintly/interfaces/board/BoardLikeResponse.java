package com.vintly.interfaces.board;

import io.swagger.v3.oas.annotations.media.Schema;

public class BoardLikeResponse {

    public record BoardLike(
            @Schema(description = "좋아요 여부")
            boolean liked,

            @Schema(description = "좋아요 수")
            long likeCount
    ) {}
}
