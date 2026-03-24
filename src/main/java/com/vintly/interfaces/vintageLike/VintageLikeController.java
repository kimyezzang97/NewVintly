package com.vintly.interfaces.vintagelike;

import com.vintly.domain.vintagelike.service.VintageLikeService;
import com.vintly.infra.config.swagger.api.SwaggerVintageLikeApi;
import com.vintly.interfaces.presentation.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vintages/{vintageId}/likes")
@RequiredArgsConstructor
@Validated
public class VintageLikeController implements SwaggerVintageLikeApi {

    private final VintageLikeService vintageLikeService;

    @PostMapping
    public ApiResponse<VintageLikeResponse.VintageLike> like(@PathVariable Long vintageId) {
        return new ApiResponse<>(true, 200, "", vintageLikeService.like(vintageId));
    }

    @DeleteMapping
    public ApiResponse<VintageLikeResponse.VintageLike> unlike(@PathVariable Long vintageId) {
        return new ApiResponse<>(true, 200, "", vintageLikeService.unlike(vintageId));
    }

    @GetMapping
    public VintageLikeResponse.VintageLike get(@PathVariable Long vintageId) {
        return vintageLikeService.getStatus(vintageId);
    }

}
