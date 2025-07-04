package com.vintly.infra.config.swagger.api;

import com.vintly.interfaces.presentation.ApiResponse;
import com.vintly.interfaces.vintage.VintageRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

public interface SwaggerVintageApi {

    @Operation(summary = "빈티지 매장 등록", description = "빈티지 매장을 등록합니다.")
    public ApiResponse<?> createVintage(@ModelAttribute VintageRequest.CreateVintage createVintage);

    @Operation(summary = "빈티지 매장 전체 조회", description = "빈티지 매장 전체 리스트 정보를 조회합니다.")
    public ApiResponse<?> getVintageList();
}
