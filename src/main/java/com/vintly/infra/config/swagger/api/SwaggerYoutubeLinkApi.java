package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "YoutubeLink", description = "빈티지 관련 유튜브 링크 API")
public interface SwaggerYoutubeLinkApi {

    @Operation(summary = "유튜브 링크 목록 조회", description = "등록된 빈티지 관련 유튜브 링크 목록을 페이지네이션으로 조회합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> getYoutubeLinkList(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );
}
