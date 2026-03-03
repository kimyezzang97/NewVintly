package com.vintly.interfaces.vintageComment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VintageCommentRequest {

    // 빈티지 매장 댓글 등록
    public record Create(
            @Schema(description = "빈티지 매장 ID", example = "42")
            @NotNull(message = "빈티지 매장 ID는 필수입니다.")
            Long vintageId,

            @Schema(description = "상위 댓글 ID (0이면 최상위 댓글)", example = "0")
            Long parentCommentId,

            @Schema(description = "댓글 내용", example = "이 매장 분위기 정말 좋아요!")
            @NotBlank(message = "댓글을 입력해주세요.")
            String comment
    ) {}

    // 빈티지 매장 댓글 수정
    public record Update(
            @Schema(description = "댓글 ID", example = "101")
            @NotNull(message = "댓글 ID는 필수입니다.")
            Long commentId,

            @Schema(description = "수정할 댓글 내용", example = "가격대비 만족도가 높아요!")
            @NotBlank(message = "수정할 내용을 입력해주세요.")
            String comment
    ) {}

    // 빈티지 매장 댓글 삭제
}
