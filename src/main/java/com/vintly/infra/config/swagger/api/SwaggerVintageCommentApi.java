package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.vintageComment.VintageCommentRequest;
import com.vintly.interfaces.vintageComment.VintageCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "VintageComment", description = "빈티지 매장 댓글 관련 API")
public interface SwaggerVintageCommentApi {

    @Operation(summary = "댓글 등록", description = "빈티지 매장에 댓글을 등록합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<VintageCommentResponse.Create> createComment(
            @PathVariable Long vintageId,
            @RequestBody @Validated VintageCommentRequest.Create req
    );

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> updateComment(
            @PathVariable Long vintageId,
            @PathVariable Long commentId,
            @RequestBody @Validated VintageCommentRequest.Update req
    );

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> deleteComment(
            @PathVariable Long vintageId,
            @PathVariable Long commentId
    );
}
