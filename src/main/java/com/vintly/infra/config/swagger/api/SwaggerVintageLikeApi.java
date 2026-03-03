package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.vintageLike.VintageLikeResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface SwaggerVintageLikeApi {

    @Operation(summary = "빈티지 매장 좋아요 등록", description = "빈티지 매장에 좋아요를 등록합니다.")
    public ApiResponse<VintageLikeResponse.VintageLike> like(@PathVariable Long vintageId);

    @Operation(summary = "빈티지 매장 좋아요 취소", description = "빈티지 매장에 좋아요를 취소합니다.")
    public ApiResponse<VintageLikeResponse.VintageLike> unlike(@PathVariable Long vintageId);

    @Operation(summary = "빈티지 매장 좋아요 조회", description = "빈티지 매장에 좋아요를 조회합니다.")
    public VintageLikeResponse.VintageLike get(@PathVariable Long vintageId);
}
