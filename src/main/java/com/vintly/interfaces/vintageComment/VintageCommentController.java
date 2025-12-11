package com.vintly.interfaces.vintageComment;

import com.vintly.domain.vintagecomment.service.VintageCommentService;
import com.vintly.domain.vintagelike.service.VintageLikeService;
import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.vintageLike.VintageLikeResponse;
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
    public ApiResponse<VintageLikeResponse.VintageLike> createComment(@PathVariable Long vintageId,
                                                                      @RequestParam(defaultValue = "0") Long parentCommentId,
                                                                      @RequestBody @Validated VintageCommentRequest.Create req) {
        return new ApiResponse<>(true, 200, "",null);
    }
}
