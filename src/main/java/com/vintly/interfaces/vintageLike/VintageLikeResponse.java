package com.vintly.interfaces.vintageLike;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VintageLikeResponse {

    // 빈티지 좋아요
    public record VintageLike(

            @Schema(description = "좋아요 여부")
            boolean liked,

            @Schema(description = "좋아요 수")
            long likeCount
    ){}

}
