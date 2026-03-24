package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.vintage.VintageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Vintage", description = "빈티지 매장 관련 API")
public interface SwaggerVintageApi {

    @Operation(summary = "빈티지 매장 등록", description = "빈티지 매장을 등록합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> createVintage(@ModelAttribute VintageRequest.CreateVintage createVintage);

    @Operation(summary = "빈티지 매장 전체 조회", description = "빈티지 매장 전체 리스트 정보를 조회합니다.")
    ApiResponse<?> getVintageList();

    @Operation(summary = "빈티지 매장 상세 조회", description = "빈티지 매장 1개를 상세 조회합니다.")
    ApiResponse<?> getVintage(@PathVariable Long id);

    @Operation(summary = "빈티지 매장 수정", description = "빈티지 매장 정보를 수정합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> updateVintage(@PathVariable Long id, @ModelAttribute VintageRequest.UpdateVintage request);

    @Operation(summary = "빈티지 매장 삭제", description = "빈티지 매장 1개를 삭제합니다.", security = @SecurityRequirement(name = "access"))
    ApiResponse<?> deleteVintage(@PathVariable Long id);

    @Operation(summary = "빈티지 매장 지역 검색", description = "시/도와 시/구로 빈티지 매장을 검색합니다.")
    ApiResponse<?> searchByLocation(
            @Parameter(description = "시/도 (예: 서울특별시)", example = "서울특별시") @RequestParam String state,
            @Parameter(description = "시/구 (예: 마포구)", example = "마포구") @RequestParam String district
    );
}
