package com.vintly.interfaces.vintageComment;

import com.vintly.domain.vintagecomment.service.VintageCommentService;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.presentation.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vintages/{vintageId}/comments")
@RequiredArgsConstructor
@Validated
public class VintageCommentController {

    private final VintageCommentService vintageCommentService;

    @PostMapping
    public ApiResponse<VintageCommentResponse.Create> createComment(@PathVariable Long vintageId,
                                                                    @RequestBody @Validated VintageCommentRequest.Create req) {
        Long commentId = vintageCommentService.create(vintageId, SecurityUtil.getCurrentMemberId(), req.parentCommentId(), req.comment());
        Long parentId = req.parentCommentId() != null ? req.parentCommentId() : 0L;
        return new ApiResponse<>(true, 200, "", new VintageCommentResponse.Create(commentId, parentId));
    }
}
