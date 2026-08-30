package com.vintly.interfaces.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardCommentRequest {

    @Schema(name = "BoardCommentCreate")
    public record Create(
            Long parentId,

            @NotBlank
            @Size(max = 500)
            String comment
    ) {}

    @Schema(name = "BoardCommentUpdate")
    public record Update(
            @NotBlank
            @Size(max = 500)
            String comment
    ) {}
}
